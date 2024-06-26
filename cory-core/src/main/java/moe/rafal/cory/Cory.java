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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.message.packet.PacketListenerDelegate;

public interface Cory {

  <T extends Packet> void publish(String channelName, T packet);

  <T extends Packet> void observe(String channelName, PacketListenerDelegate<T> packetListener);

  <T extends Packet> void mutualObserve(
      String channelName, PacketListenerDelegate<T> packetListener);

  <T extends Packet, R extends Packet> CompletableFuture<R> request(String channelName, T packet);

  void close() throws IOException;
}
