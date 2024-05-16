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

import java.io.IOException;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.logger.impl.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketPackerFactory;

class PacketPublisherImpl implements PacketPublisher {

  private static final String PACKET_PUBLISHING_STARTING =
      "Publishing packet of type %s (%s) over the channel %s.";
  private static final String PACKET_PUBLISHING_COMPLETED =
      "Packet of type %s (%s) has been published over the %s channel with payload of %d bytes.";
  private final LoggerFacade loggerFacade;
  private final MessageBroker messageBroker;
  private final PacketGateway packetGateway;
  private final PacketPackerFactory packetPackerFactory;

  PacketPublisherImpl(
      LoggerFacade loggerFacade,
      MessageBroker messageBroker,
      PacketGateway packetGateway,
      PacketPackerFactory packetPackerFactory) {
    this.loggerFacade = loggerFacade;
    this.messageBroker = messageBroker;
    this.packetGateway = packetGateway;
    this.packetPackerFactory = packetPackerFactory;
  }

  @Override
  public <T extends Packet> void publish(String channelName, T packet) {
    logPacketPublicationStart(packet, channelName);
    try (PacketPacker packer = packetPackerFactory.getPacketPacker()) {
      packetGateway.writePacket(packet, packer);
      final byte[] payload = packer.toBinaryArray();
      messageBroker.publish(channelName, payload);
      logPacketPublicationCompletion(packet, channelName, payload);
    } catch (IOException exception) {
      throw new PacketPublicationException(
          "Could not publish packet over the message broker, because of unexpected exception.",
          exception);
    }
  }

  private void logPacketPublicationStart(Packet packet, String channelName) {
    loggerFacade.log(
        FINEST,
        PACKET_PUBLISHING_STARTING,
        packet.getClass().getSimpleName(),
        packet.getUniqueId(),
        channelName);
  }

  private void logPacketPublicationCompletion(Packet packet, String channelName, byte[] payload) {
    loggerFacade.log(
        FINEST,
        PACKET_PUBLISHING_COMPLETED,
        packet.getClass().getSimpleName(),
        packet.getUniqueId(),
        channelName,
        payload.length);
  }
}
