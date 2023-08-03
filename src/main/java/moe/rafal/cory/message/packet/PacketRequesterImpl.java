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
import java.util.function.Consumer;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketPackerFactory;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.serdes.PacketUnpackerFactory;

class PacketRequesterImpl implements PacketRequester {

  private final MessageBroker messageBroker;
  private final PacketGateway packetGateway;

  PacketRequesterImpl(MessageBroker broker, PacketGateway packetGateway) {
    this.messageBroker = broker;
    this.packetGateway = packetGateway;
  }

  @Override
  public <T extends Packet, R extends Packet> void request(String channelName, T packet,
      Consumer<R> callback) {
    try (PacketPacker packer = PacketPackerFactory.producePacketPacker()) {
      packetGateway.writePacket(packet, packer);
      messageBroker.request(channelName, packer.toBinaryArray(), bytes -> handle(bytes, callback));
    } catch (IOException exception) {
      throw new PacketPublicationException(
          "Could not publish packet over the message broker, because of unexpected exception",
          exception);
    }
  }

  @Override
  public <R extends Packet> void handle(byte[] message, Consumer<R> callback) {
    try (PacketUnpacker packetUnpacker = PacketUnpackerFactory.producePacketUnpacker(message)) {
      R response = packetGateway.readPacket(packetUnpacker);
      callback.accept(response);
    } catch (Exception exception) {
      throw new PacketProcessingException(
          "Could not process incoming packet, because of unexpected exception.",
          exception);
    }
  }
}
