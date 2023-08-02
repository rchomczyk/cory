package moe.rafal.cory.packet;

import java.io.IOException;
import moe.rafal.cory.packet.serdes.PacketPacker;

interface PacketWriter {

  <T extends Packet> void writePacket(T packet, PacketPacker packer)
      throws IOException;

  <T extends Packet> void writePacketType(T packet, PacketPacker packer)
      throws IOException;

  <T extends Packet> void writePacketUniqueId(T packet, PacketPacker packer)
      throws IOException;
}
