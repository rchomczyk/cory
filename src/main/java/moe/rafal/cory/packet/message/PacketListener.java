package moe.rafal.cory.packet.message;

import moe.rafal.cory.packet.Packet;

@FunctionalInterface
public interface PacketListener<T extends Packet> {

  void receive(byte[] channelName, T packet);
}
