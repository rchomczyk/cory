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

import static moe.rafal.cory.logger.LoggerFacade.getNoopLogger;
import static moe.rafal.cory.message.packet.PacketListenerObserver.getPacketListenerObserver;
import static moe.rafal.cory.message.packet.PacketPublisher.getPacketPublisher;
import static moe.rafal.cory.message.packet.PacketRequester.getPacketRequester;

import moe.rafal.cory.logger.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.packet.PacketListenerObserver;
import moe.rafal.cory.message.packet.PacketPublisher;
import moe.rafal.cory.message.packet.PacketRequester;
import moe.rafal.cory.serdes.PacketSerdesContext;

public final class CoryBuilder {

  private LoggerFacade loggerFacade = getNoopLogger();
  private MessageBroker messageBroker;
  private PacketSerdesContext serdesContext;

  private CoryBuilder() {}

  public static CoryBuilder newBuilder() {
    return new CoryBuilder();
  }

  public CoryBuilder withLoggerFacade(LoggerFacade loggerFacade) {
    this.loggerFacade = loggerFacade;
    return this;
  }

  public CoryBuilder withMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
    return this;
  }

  public CoryBuilder withSerdesContext(PacketSerdesContext serdesContext) {
    this.serdesContext = serdesContext;
    return this;
  }

  public Cory build() throws CoryBuildException {
    if (messageBroker == null) {
      throw new CoryBuildException(
          "Cory could not be built, because of missing message broker, which is required for proper functioning.");
    }

    if (serdesContext == null) {
      throw new CoryBuildException(
          "Cory could not be built, because of missing packet serdes context.");
    }

    final PacketGateway packetGateway = PacketGateway.INSTANCE;
    final PacketPublisher packetPublisher =
        getPacketPublisher(loggerFacade, messageBroker, packetGateway, serdesContext);
    final PacketRequester packetRequester =
        getPacketRequester(loggerFacade, messageBroker, packetGateway, serdesContext);
    final PacketListenerObserver packetListenerObserver =
        getPacketListenerObserver(
            loggerFacade, messageBroker, packetGateway, packetPublisher, serdesContext);
    return new CoryImpl(
        loggerFacade, messageBroker, packetPublisher, packetRequester, packetListenerObserver);
  }
}
