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

package moe.rafal.cory;

import static moe.rafal.cory.message.packet.PacketListenerObserverFactory.producePacketListenerObserver;
import static moe.rafal.cory.message.packet.PacketPublisherFactory.producePacketPublisher;
import static moe.rafal.cory.message.packet.PacketRequesterFactory.producePacketRequester;

import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.packet.PacketPublisher;
import moe.rafal.cory.message.packet.PacketRequester;
import moe.rafal.cory.serdes.PacketPackerFactory;
import moe.rafal.cory.serdes.PacketUnpackerFactory;

public final class CoryBuilder {

  private MessageBroker messageBroker;
  private PacketPackerFactory packetPackerFactory;
  private PacketUnpackerFactory packetUnpackerFactory;

  private CoryBuilder() {

  }

  public static CoryBuilder newBuilder() {
    return new CoryBuilder();
  }

  public CoryBuilder withMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
    return this;
  }

  public CoryBuilder withPacketPackerFactory(PacketPackerFactory packetPackerFactory) {
    this.packetPackerFactory = packetPackerFactory;
    return this;
  }

  public CoryBuilder withPacketUnpackerFactory(PacketUnpackerFactory packetUnpackerFactory) {
    this.packetUnpackerFactory = packetUnpackerFactory;
    return this;
  }

  public Cory build() throws CoryBuildException {
    if (messageBroker == null) {
      throw new CoryBuildException(
          "Cory could not be built, because of missing message broker, which is required for proper functioning.");
    }

    if (packetPackerFactory == null) {
      throw new CoryBuildException(
          "Cory could not be built, because of missing packet packer factory.");
    }

    if (packetUnpackerFactory == null) {
      throw new CoryBuildException(
          "Cory could not be built, because of missing packet unpacker factory.");
    }

    PacketGateway packetGateway = PacketGateway.INSTANCE;
    PacketPublisher packetPublisher = producePacketPublisher(
        messageBroker, packetGateway, packetPackerFactory);
    PacketRequester packetRequester = producePacketRequester(
        messageBroker, packetGateway, packetPackerFactory, packetUnpackerFactory);
    return new CoryImpl(
        messageBroker,
        packetPublisher,
        packetRequester,
        producePacketListenerObserver(
            messageBroker, packetGateway, packetPublisher, packetUnpackerFactory));
  }
}
