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

import static moe.rafal.cory.serdes.PacketUnpackerFactory.producePacketUnpacker;

import java.io.IOException;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.jacoco.ExcludeFromJacocoGeneratedReport;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketUnpacker;

class PacketListenerObserverImpl implements PacketListenerObserver {

  private final MessageBroker messageBroker;
  private final PacketGateway packetGateway;

  PacketListenerObserverImpl(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
    this.packetGateway = PacketGateway.INSTANCE;
  }

  @Override
  public <T extends Packet> void observe(String channelName,
      PacketListenerDelegate<T> packetListener) {
    messageBroker.observe(channelName,
        (ignored, payload) -> {
          Packet packet = processIncomingPacket(payload);

          boolean whetherListensForPacket = packetListener.getPacketType()
              .equals(packet.getClass());
          if (whetherListensForPacket) {
            // noinspection unchecked
            packetListener.receive(channelName, (T) packet);
          }
        });
  }

  @ExcludeFromJacocoGeneratedReport
  private <T extends Packet> T processIncomingPacket(byte[] payload)
      throws PacketProcessingException {
    try (PacketUnpacker unpacker = producePacketUnpacker(payload)) {
      return packetGateway.readPacket(unpacker);
    } catch (IOException exception) {
      throw new PacketProcessingException(
          "Could not process incoming packet, because of unexpected exception.",
          exception);
    }
  }
}
