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

import static moe.rafal.cory.packet.message.PacketListenerRegistryFactory.producePacketListenerRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import moe.rafal.cory.packet.Packet;
import moe.rafal.cory.packet.subject.LoginPacketListener;
import org.junit.jupiter.api.Test;

class MapBasedPacketListenerRegistryTests {

  private final PacketListener<?> listener = new LoginPacketListener();

  @Test
  void registerPacketListenerTest() {
    PacketListenerRegistry packetListenerRegistry = producePacketListenerRegistry();
    packetListenerRegistry.register(listener);
    assertThat(packetListenerRegistry.getPacketListenersByPacketType(listener.getPacketType()))
        .containsExactlyInAnyOrder(listener);
  }

  @Test
  void registerPacketListenersWithDuplicatesTest() {
    LoginPacketListener listener = new LoginPacketListener();
    LoginPacketListener listenerDuplicate = new LoginPacketListener();
    PacketListenerRegistry packetListenerRegistry = producePacketListenerRegistry();
    packetListenerRegistry.register(listener);
    packetListenerRegistry.register(listenerDuplicate);
    assertThat(packetListenerRegistry.getPacketListenersByPacketType(listener.getPacketType()))
        .containsExactlyInAnyOrder(listener, listenerDuplicate);
  }

  @Test
  void getPacketListenersByPacketTypeTest() {
    PacketListenerRegistry packetListenerRegistry = producePacketListenerRegistry();
    packetListenerRegistry.register(listener);
    assertThat(packetListenerRegistry.getPacketListenersByPacketType(listener.getPacketType()))
        .containsExactlyInAnyOrder(listener);
    assertThat(packetListenerRegistry.getPacketListenersByPacketType(Packet.class))
        .isEmpty();
  }
}
