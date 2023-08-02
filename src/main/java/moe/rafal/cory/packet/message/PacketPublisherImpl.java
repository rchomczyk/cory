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

package moe.rafal.cory.packet.message;

import static moe.rafal.cory.packet.serdes.PacketPackerFactory.producePacketPacker;

import java.io.IOException;
import moe.rafal.cory.packet.Packet;
import moe.rafal.cory.packet.PacketGateway;
import moe.rafal.cory.packet.serdes.PacketPacker;

class PacketPublisherImpl implements PacketPublisher {

  private final PacketGateway packetGateway;
  private final MessageBroker messageBroker;

  PacketPublisherImpl(MessageBroker messageBroker) {
    this.packetGateway = PacketGateway.INSTANCE;
    this.messageBroker = messageBroker;
  }

  @Override
  public <T extends Packet> void publish(String channelName, T packet) {
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writePacket(packet, packer);
      messageBroker.publish(channelName, packer.toBinaryArray());
    } catch (IOException exception) {
      throw new PacketPublicationException(
          "Could not publish packet over the message broker, because of unexpected exception",
          exception);
    }
  }
}
