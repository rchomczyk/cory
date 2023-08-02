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
