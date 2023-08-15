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
import java.util.concurrent.CompletableFuture;
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
  private final PacketPackerFactory packetPackerFactory;
  private final PacketUnpackerFactory packetUnpackerFactory;


  PacketRequesterImpl(
      MessageBroker messageBroker,
      PacketGateway packetGateway,
      PacketPackerFactory packetPackerFactory,
      PacketUnpackerFactory packetUnpackerFactory) {
    this.messageBroker = messageBroker;
    this.packetGateway = packetGateway;
    this.packetPackerFactory = packetPackerFactory;
    this.packetUnpackerFactory = packetUnpackerFactory;
  }

  @Override
  public <T extends Packet, R extends Packet> CompletableFuture<R> request(String channelName,
      T packet) {
    try (PacketPacker packer = packetPackerFactory.producePacketPacker()) {
      packetGateway.writePacket(packet, packer);
      return messageBroker.request(channelName, packer.toBinaryArray())
          .thenCompose(this::processIncomingPacket);
    } catch (IOException exception) {
      throw new PacketPublicationException(
          "Could not request packet over the message broker, because of unexpected exception.",
          exception);
    }
  }

  @Override
  public <T extends Packet> CompletableFuture<T> processIncomingPacket(byte[] message)
      throws PacketProcessingException {
    return CompletableFuture
        .supplyAsync(() -> {
          try (PacketUnpacker unpacker = packetUnpackerFactory.producePacketUnpacker(message)) {
            return packetGateway.<T>readPacket(unpacker);
          } catch (Exception exception) {
            throw new PacketProcessingException(
                "Could not process incoming request packet, because of unexpected exception.",
                exception);
          }
        })
        .exceptionally(exception -> {
          throw new PacketProcessingException(
              "Could not complete processing of incoming request packet, because of unexpected exception.",
              exception);
        });
  }
}
