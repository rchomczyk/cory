package moe.rafal.cory.packet;

import java.io.IOException;
import java.util.UUID;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

interface PacketReader {

  <T extends Packet> T readPacket(PacketUnpacker unpacker)
      throws IOException, MalformedPacketException;

  <T extends Packet> Class<T> readPacketType(PacketUnpacker unpacker)
      throws IOException, MalformedPacketException;

  UUID readPacketUniqueId(PacketUnpacker unpacker)
      throws IOException;
}
