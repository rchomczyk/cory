package moe.rafal.cory.packet.message;

import moe.rafal.cory.packet.Packet;

public interface PacketListenerRegistry {

  <T extends Packet> void register(String channelName, PacketListener<T> listener);
}
