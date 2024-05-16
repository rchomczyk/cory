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

package moe.rafal.cory;

import static java.util.logging.Level.FINER;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.logger.impl.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.packet.PacketListener;
import moe.rafal.cory.message.packet.PacketListenerDelegate;
import moe.rafal.cory.message.packet.PacketListenerObserver;
import moe.rafal.cory.message.packet.PacketPublisher;
import moe.rafal.cory.message.packet.PacketRequester;

class CoryImpl implements Cory {

  private static final String CHANNEL_OBSERVED =
      "Channel %s is now being observed by %s listener for incoming packets.";
  private static final String CHANNEL_OBSERVED_WITH_PROCESSING =
      "Channel %s is now being observed by %s listener for incoming packets to process.";
  private final LoggerFacade loggerFacade;
  private final MessageBroker messageBroker;
  private final PacketPublisher packetPublisher;
  private final PacketRequester packetRequester;
  private final PacketListenerObserver packetListenerObserver;

  CoryImpl(
      LoggerFacade loggerFacade,
      MessageBroker messageBroker,
      PacketPublisher packetPublisher,
      PacketRequester packetRequester,
      PacketListenerObserver packetListenerObserver) {
    this.loggerFacade = loggerFacade;
    this.messageBroker = messageBroker;
    this.packetPublisher = packetPublisher;
    this.packetRequester = packetRequester;
    this.packetListenerObserver = packetListenerObserver;
  }

  @Override
  public <T extends Packet> void publish(String channelName, T packet) {
    packetPublisher.publish(channelName, packet);
  }

  @Override
  public <T extends Packet> void observe(
      String channelName, PacketListenerDelegate<T> packetListener) {
    logChannelObservation(channelName, packetListener);
    packetListenerObserver.observe(channelName, packetListener);
  }

  @Override
  public <T extends Packet> void observeWithProcessing(
      String channelName, PacketListenerDelegate<T> packetListener) {
    logChannelObservationWithProcessing(channelName, packetListener);
    packetListenerObserver.observeWithProcessing(channelName, packetListener);
  }

  @Override
  public <T extends Packet, R extends Packet> CompletableFuture<R> request(
      String channelName, T packet) {
    return packetRequester.request(channelName, packet);
  }

  @Override
  public void close() throws IOException {
    messageBroker.close();
  }

  private void logChannelObservation(String channelName, PacketListener<?> packetListener) {
    loggerFacade.log(FINER, CHANNEL_OBSERVED, channelName, packetListener.getClass().getName());
  }

  private void logChannelObservationWithProcessing(
      String channelName, PacketListener<?> packetListener) {
    loggerFacade.log(
        FINER, CHANNEL_OBSERVED_WITH_PROCESSING, channelName, packetListener.getClass().getName());
  }
}
