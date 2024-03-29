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

import java.io.IOException;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketPackerFactory;

class PacketPublisherImpl implements PacketPublisher {

  private final MessageBroker messageBroker;
  private final PacketGateway packetGateway;
  private final PacketPackerFactory packetPackerFactory;

  PacketPublisherImpl(MessageBroker messageBroker, PacketGateway packetGateway, PacketPackerFactory packetPackerFactory) {
    this.messageBroker = messageBroker;
    this.packetGateway = packetGateway;
    this.packetPackerFactory = packetPackerFactory;
  }

  @Override
  public <T extends Packet> void publish(String channelName, T packet) {
    try (PacketPacker packer = packetPackerFactory.producePacketPacker()) {
      packetGateway.writePacket(packet, packer);
      messageBroker.publish(channelName, packer.toBinaryArray());
    } catch (IOException exception) {
      throw new PacketPublicationException(
          "Could not publish packet over the message broker, because of unexpected exception.",
          exception);
    }
  }
}
