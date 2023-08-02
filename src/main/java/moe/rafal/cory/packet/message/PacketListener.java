package moe.rafal.cory.packet.message;

import moe.rafal.cory.packet.Packet;

public interface PacketListener<T extends Packet> {

  void receive(String channelName, T packet);

  Class<T> getPacketType();
}
