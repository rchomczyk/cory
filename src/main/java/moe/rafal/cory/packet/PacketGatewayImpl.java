package moe.rafal.cory.packet;

import java.io.IOException;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

class PacketGatewayImpl implements PacketGateway {

  @Override
  public <T extends Packet> T mutate(Class<T> packetType, Packet packet) {
    return packetType.cast(packet);
  }

  @Override
  public <T extends Packet> void writeDefinition(Class<T> packetType, PacketPacker packer)
      throws IOException {
    packer.packString(packetType.getName());
  }

  @Override
  public <T extends Packet> Class<T> readDefinition(PacketUnpacker unpacker)
      throws IOException, PacketMalformedDefinitionException {
    try {
      // noinspection unchecked
      return (Class<T>) Class.forName(unpacker.unpackString());
    } catch (ClassNotFoundException exception) {
      throw new PacketMalformedDefinitionException(
          "Packet definition seems to be malformed, as written class could not be found in classpath.",
          exception);
    }
  }
}
