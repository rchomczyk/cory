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

public final class CoryBuilder {

  private MessageBroker messageBroker;

  private CoryBuilder() {

  }

  public static CoryBuilder newBuilder() {
    return new CoryBuilder();
  }

  public CoryBuilder withMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
    return this;
  }

  public Cory build() {
    PacketGateway packetGateway = PacketGateway.INSTANCE;
    return new CoryImpl(
        messageBroker,
        producePacketPublisher(messageBroker, packetGateway),
        producePacketRequester(messageBroker, packetGateway),
        producePacketListenerObserver(messageBroker, packetGateway));
  }
}
