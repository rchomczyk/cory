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

import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.logger.impl.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketPackerFactory;

@FunctionalInterface
public interface PacketPublisher {

  static PacketPublisher getPacketPublisher(
      LoggerFacade loggerFacade,
      MessageBroker messageBroker,
      PacketGateway packetGateway,
      PacketPackerFactory packetPackerFactory) {
    return new PacketPublisherImpl(loggerFacade, messageBroker, packetGateway, packetPackerFactory);
  }

  <T extends Packet> void publish(String channelName, T packet) throws PacketPublicationException;
}
