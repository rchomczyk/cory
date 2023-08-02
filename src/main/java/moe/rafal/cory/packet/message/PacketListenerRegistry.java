package moe.rafal.cory.packet.message;

import java.util.Set;
import moe.rafal.cory.packet.Packet;

public interface PacketListenerRegistry {

  <T extends Packet> void register(PacketListener<T> listener);

  <T extends Packet> Set<PacketListener<? extends Packet>> getPacketListenersByPacketType(
      Class<T> packetType);
}
