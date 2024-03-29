/*
 *    Copyright 2023 cory
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package moe.rafal.cory.message.packet;

import java.util.concurrent.CompletionStage;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.serdes.PacketUnpackerFactory;

class PacketListenerObserverImpl implements PacketListenerObserver {

  private final MessageBroker messageBroker;
  private final PacketGateway packetGateway;
  private final PacketPublisher packetPublisher;
  private final PacketUnpackerFactory packetUnpackerFactory;

  PacketListenerObserverImpl(
      MessageBroker messageBroker,
      PacketGateway packetGateway,
      PacketPublisher packetPublisher,
      PacketUnpackerFactory packetUnpackerFactory) {
    this.messageBroker = messageBroker;
    this.packetGateway = packetGateway;
    this.packetPublisher = packetPublisher;
    this.packetUnpackerFactory = packetUnpackerFactory;
  }

  @Override
  public <T extends Packet> void observe(String channelName,
      PacketListenerDelegate<T> packetListener) {
    messageBroker.observe(channelName, (ignored, replyChannelName, payload) -> {
      Packet packet = processIncomingPacket(payload);

      boolean whetherListensForPacket = packetListener.getPacketType()
          .equals(packet.getClass());
      if (whetherListensForPacket) {
        // noinspection unchecked
        packetListener.receive(channelName, replyChannelName, (T) packet);
      }
    });
  }

  @Override
  public <T extends Packet> void observeWithProcessing(String channelName,
      PacketListenerDelegate<T> packetListener) {
    messageBroker.observe(channelName, (ignored, replyChannelName, payload) -> {
      Packet requestPacket = processIncomingPacket(payload);

      boolean whetherListensForPacket = packetListener.getPacketType()
          .equals(requestPacket.getClass());
      if (whetherListensForPacket) {
        String gotoChannelName =
            packetListener.isPublishedOnReplyChannel() ? replyChannelName : channelName;
        // noinspection unchecked
        Object processingResult = packetListener.process(channelName, replyChannelName,
            (T) requestPacket);
        if (processingResult instanceof Packet) {
          packetPublisher.publish(gotoChannelName, (Packet) processingResult);
        } else if (processingResult instanceof CompletionStage) {
          ((CompletionStage<?>) processingResult)
              .thenAccept(packet -> packetPublisher.publish(gotoChannelName, (Packet) packet))
              .exceptionally(exception -> {
                throw new PacketPublicationException(
                    "Could not publish processed packet, because of unexpected exception.",
                    exception);
              });
        }
      }
    });
  }

  @Override
  public <T extends Packet> T processIncomingPacket(byte[] payload)
      throws PacketProcessingException {
    try (PacketUnpacker unpacker = packetUnpackerFactory.producePacketUnpacker(payload)) {
      return packetGateway.readPacket(unpacker);
    } catch (Exception exception) {
      throw new PacketProcessingException(
          "Could not process incoming packet, because of unexpected exception.",
          exception);
    }
  }
}
