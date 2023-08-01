package moe.rafal.cory.packet.message;

import moe.rafal.cory.packet.Packet;

@FunctionalInterface
public interface PacketListener<T extends Packet> {

  void receive(String channelName, T packet);
}
