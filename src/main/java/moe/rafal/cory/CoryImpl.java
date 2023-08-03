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

import java.io.IOException;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.packet.PacketListenerDelegate;
import moe.rafal.cory.message.packet.PacketListenerObserver;
import moe.rafal.cory.message.packet.PacketPublisher;

class CoryImpl implements Cory {

  private final MessageBroker messageBroker;
  private final PacketPublisher packetPublisher;
  private final PacketListenerObserver packetListenerObserver;

  CoryImpl(MessageBroker messageBroker, PacketPublisher packetPublisher,
      PacketListenerObserver packetListenerObserver) {
    this.messageBroker = messageBroker;
    this.packetPublisher = packetPublisher;
    this.packetListenerObserver = packetListenerObserver;
  }

  @Override
  public <T extends Packet> void publish(String channelName, T packet) {
    packetPublisher.publish(channelName, packet);
  }

  @Override
  public <T extends Packet> void observe(String channelName,
      PacketListenerDelegate<T> packetListener) {
    packetListenerObserver.observe(channelName, packetListener);
  }

  @Override
  public void close() throws IOException {
    messageBroker.close();
  }
}
