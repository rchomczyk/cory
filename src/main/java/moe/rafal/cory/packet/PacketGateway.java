package moe.rafal.cory.packet;

import java.io.IOException;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

public interface PacketGateway {

  <T extends Packet> T mutate(Class<T> packetType, Packet packet);

  <T extends Packet> void writeDefinition(Class<T> packetType, PacketPacker packer)
      throws IOException;

  <T extends Packet> Class<T> readDefinition(PacketUnpacker unpacker)
      throws IOException, PacketMalformedDefinitionException;
}
