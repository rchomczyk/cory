package moe.rafal.cory.packet.message;

import moe.rafal.cory.packet.Packet;

public interface PacketPublisher {

  <T extends Packet> void publish(byte[] channelName, T packet);
}
