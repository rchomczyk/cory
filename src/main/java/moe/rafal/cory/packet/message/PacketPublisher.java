package moe.rafal.cory.packet.message;

import moe.rafal.cory.packet.Packet;

@FunctionalInterface
public interface PacketPublisher {

  <T extends Packet> void publish(String channelName, T packet) throws PacketPublicationException;
}
