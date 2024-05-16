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

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.logging.Level.FINEST;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.logger.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketPackerFactory;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.serdes.PacketUnpackerFactory;
import pl.auroramc.commons.concurrent.CompletableFutureUtils;

class PacketRequesterImpl implements PacketRequester {

  private static final String PACKET_REQUESTING_STARTING =
      "Requesting packet of type %s (%s) over the channel %s.";
  private static final String PACKET_REQUESTING_COMPLETED =
      "Request of packet of type %s (%s) has been completed over the %s channel with payload of %d bytes.";
  private static final String PACKET_REQUESTING_FULFILLED =
      "Request of packet of type %s (%s) has been fulfilled over the temporary channel with payload of %d bytes.";
  private final LoggerFacade loggerFacade;
  private final MessageBroker messageBroker;
  private final PacketGateway packetGateway;
  private final PacketPackerFactory packetPackerFactory;
  private final PacketUnpackerFactory packetUnpackerFactory;

  PacketRequesterImpl(
      LoggerFacade loggerFacade,
      MessageBroker messageBroker,
      PacketGateway packetGateway,
      PacketPackerFactory packetPackerFactory,
      PacketUnpackerFactory packetUnpackerFactory) {
    this.loggerFacade = loggerFacade;
    this.messageBroker = messageBroker;
    this.packetGateway = packetGateway;
    this.packetPackerFactory = packetPackerFactory;
    this.packetUnpackerFactory = packetUnpackerFactory;
  }

  @Override
  public <T extends Packet, R extends Packet> CompletableFuture<R> request(
      String channelName, T packet) {
    logPacketRequestingStart(packet, channelName);
    try (PacketPacker packer = packetPackerFactory.getPacketPacker()) {
      packetGateway.writePacket(packet, packer);
      final byte[] payload = packer.toBinaryArray();
      logPacketRequestingCompletion(packet, channelName, payload);
      return messageBroker
          .request(channelName, payload)
          .<R>thenCompose(this::processIncomingPacket)
          .exceptionally(CompletableFutureUtils::delegateCaughtException);
    } catch (IOException exception) {
      throw new PacketPublicationException(
          "Could not request packet over the message broker, because of unexpected exception.",
          exception);
    }
  }

  @Override
  public <T extends Packet> CompletableFuture<T> processIncomingPacket(byte[] message)
      throws PacketProcessingException {
    return supplyAsync(
            () -> {
              try (PacketUnpacker unpacker = packetUnpackerFactory.getPacketUnpacker(message)) {
                final T receivedPacket = packetGateway.readPacket(unpacker);
                logPacketRequestingFulfilled(receivedPacket, message);
                return receivedPacket;
              } catch (Exception exception) {
                throw new PacketProcessingException(
                    "Could not process incoming request packet, because of unexpected exception.",
                    exception);
              }
            })
        .exceptionally(
            exception -> {
              throw new PacketProcessingException(
                  "Could not complete processing of incoming request packet, because of unexpected exception.",
                  exception);
            });
  }

  private void logPacketRequestingStart(Packet packet, String channelName) {
    loggerFacade.log(
        FINEST,
        PACKET_REQUESTING_STARTING,
        packet.getClass().getSimpleName(),
        packet.getUniqueId(),
        channelName);
  }

  private void logPacketRequestingCompletion(Packet packet, String channelName, byte[] payload) {
    loggerFacade.log(
        FINEST,
        PACKET_REQUESTING_COMPLETED,
        packet.getClass().getSimpleName(),
        packet.getUniqueId(),
        channelName,
        payload.length);
  }

  private void logPacketRequestingFulfilled(Packet packet, byte[] payload) {
    loggerFacade.log(
        FINEST,
        PACKET_REQUESTING_FULFILLED,
        packet.getClass().getSimpleName(),
        packet.getUniqueId(),
        payload.length);
  }
}
