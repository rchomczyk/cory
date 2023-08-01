package moe.rafal.cory.packet.message;

import moe.rafal.cory.packet.Packet;

@FunctionalInterface
public interface PacketPublisher {

  <T extends Packet> void publish(byte[] channelName, T packet);
}
