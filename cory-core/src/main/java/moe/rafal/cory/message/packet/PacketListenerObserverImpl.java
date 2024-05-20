/*
 *    Copyright 2023-2024 cory
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

import static java.util.logging.Level.FINEST;

import java.util.concurrent.CompletionStage;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.logger.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketSerdesContext;
import moe.rafal.cory.serdes.PacketUnpacker;

class PacketListenerObserverImpl implements PacketListenerObserver {

  private static final String RECEIVED_PACKET =
      "Received packet of type %s (%s) from %s channel with %s reply channel. Preview: %s";
  private static final String RECEIVED_AND_FORWARDED_PACKET =
      "Received packet of type %s (%s) from %s channel with %s reply channel and forwarded to %s listener. Preview: %s";
  private static final String RECEIVED_AND_RESPONDED_PACKET =
      "Received packet of type %s (%s) from %s channel with %s reply channel and responded with %s packet. Preview: %s";
  private final LoggerFacade loggerFacade;
  private final MessageBroker messageBroker;
  private final PacketGateway packetGateway;
  private final PacketPublisher packetPublisher;
  private final PacketSerdesContext serdesContext;

  PacketListenerObserverImpl(
      LoggerFacade loggerFacade,
      MessageBroker messageBroker,
      PacketGateway packetGateway,
      PacketPublisher packetPublisher,
      PacketSerdesContext serdesContext) {
    this.loggerFacade = loggerFacade;
    this.messageBroker = messageBroker;
    this.packetGateway = packetGateway;
    this.packetPublisher = packetPublisher;
    this.serdesContext = serdesContext;
  }

  @Override
  public <T extends Packet> void observe(
      String channelName, PacketListenerDelegate<T> packetListener) {
    messageBroker.observe(
        channelName,
        (ignored, replyChannelName, payload) -> {
          Packet packet = processIncomingPacket(payload);
          logReceivedPacket(packet, channelName, replyChannelName);

          boolean whetherListensForPacket =
              packetListener.getPacketType().equals(packet.getClass());
          if (whetherListensForPacket) {
            // noinspection unchecked
            packetListener.receive(channelName, replyChannelName, (T) packet);
            logReceivedAndForwardedPacket(packet, channelName, replyChannelName, packetListener);
          }
        });
  }

  @Override
  public <T extends Packet> void observeWithProcessing(
      String channelName, PacketListenerDelegate<T> packetListener) {
    messageBroker.observe(
        channelName,
        (ignored, replyChannelName, payload) -> {
          Packet requestPacket = processIncomingPacket(payload);
          logReceivedPacket(requestPacket, channelName, replyChannelName);

          boolean whetherListensForPacket =
              packetListener.getPacketType().equals(requestPacket.getClass());
          if (whetherListensForPacket) {
            String gotoChannelName =
                packetListener.isPublishedOnReplyChannel() ? replyChannelName : channelName;
            // noinspection unchecked
            Object processingResult =
                packetListener.process(channelName, replyChannelName, (T) requestPacket);
            if (processingResult instanceof Packet) {
              packetPublisher.publish(gotoChannelName, (Packet) processingResult);
              logReceivedAndRespondedPacket(
                  requestPacket, channelName, replyChannelName, (Packet) processingResult);
            } else if (processingResult instanceof CompletionStage) {
              ((CompletionStage<?>) processingResult)
                  .thenAccept(
                      packet -> {
                        packetPublisher.publish(gotoChannelName, (Packet) packet);
                        logReceivedAndRespondedPacket(
                            requestPacket, channelName, replyChannelName, (Packet) packet);
                      })
                  .exceptionally(
                      exception -> {
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
    try (PacketUnpacker unpacker = serdesContext.newPacketUnpacker(payload)) {
      return packetGateway.readPacket(unpacker);
    } catch (Exception exception) {
      throw new PacketProcessingException(
          "Could not process incoming packet, because of unexpected exception.", exception);
    }
  }

  private void logReceivedPacket(Packet packet, String channelName, String replyChannelName) {
    loggerFacade.log(
        FINEST,
        RECEIVED_PACKET,
        packet.getClass().getName(),
        packet.getUniqueId(),
        channelName,
        replyChannelName,
        packet);
  }

  private void logReceivedAndForwardedPacket(
      Packet packet,
      String channelName,
      String replyChannelName,
      PacketListener<?> packetListener) {
    loggerFacade.log(
        FINEST,
        RECEIVED_AND_FORWARDED_PACKET,
        packet.getClass().getName(),
        packet.getUniqueId(),
        channelName,
        replyChannelName,
        packetListener.getClass().getName(),
        packet);
  }

  private void logReceivedAndRespondedPacket(
      Packet packet, String channelName, String replyChannelName, Packet responsePacket) {
    loggerFacade.log(
        FINEST,
        RECEIVED_AND_RESPONDED_PACKET,
        packet.getClass().getName(),
        packet.getUniqueId(),
        channelName,
        replyChannelName,
        responsePacket.getClass().getName(),
        packet);
  }
}
