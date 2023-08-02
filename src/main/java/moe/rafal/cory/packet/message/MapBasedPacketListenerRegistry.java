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

package moe.rafal.cory.packet.message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import moe.rafal.cory.packet.Packet;

class MapBasedPacketListenerRegistry implements PacketListenerRegistry {

  private final Map<Class<? extends Packet>, Set<PacketListener<? extends Packet>>> listeners;

  MapBasedPacketListenerRegistry() {
    this.listeners = new HashMap<>();
  }

  @Override
  public <T extends Packet> void register(PacketListener<T> listener) {
    getPacketListenersByPacketType(listener.getPacketType()).add(listener);
  }

  @Override
  public <T extends Packet> Set<PacketListener<? extends Packet>> getPacketListenersByPacketType(
      Class<T> packetType) {
    return listeners.computeIfAbsent(packetType, key -> new HashSet<>());
  }
}
