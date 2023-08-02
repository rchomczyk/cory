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
