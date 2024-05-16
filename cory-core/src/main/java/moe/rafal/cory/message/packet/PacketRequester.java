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

import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.logger.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketPackerFactory;
import moe.rafal.cory.serdes.PacketUnpackerFactory;

public interface PacketRequester {

  static PacketRequester getPacketRequester(
      LoggerFacade loggerFacade,
      MessageBroker messageBroker,
      PacketGateway packetGateway,
      PacketPackerFactory packetPackerFactory,
      PacketUnpackerFactory packetUnpackerFactory) {
    return new PacketRequesterImpl(
        loggerFacade, messageBroker, packetGateway, packetPackerFactory, packetUnpackerFactory);
  }

  <T extends Packet, R extends Packet> CompletableFuture<R> request(String channelName, T packet);

  <T extends Packet> CompletableFuture<T> processIncomingPacket(byte[] message)
      throws PacketProcessingException;
}
