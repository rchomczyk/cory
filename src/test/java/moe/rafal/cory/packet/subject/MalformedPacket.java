package moe.rafal.cory.packet.subject;

import java.io.IOException;
import moe.rafal.cory.packet.Packet;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

public class MalformedPacket extends Packet {

  private String username;

  public MalformedPacket(String username) {
    super();
    this.username = username;
  }

  protected MalformedPacket() {
    super();
  }

  @Override
  public void write(PacketPacker packer) throws IOException {
    packer.packString(username);
  }

  @Override
  public void read(PacketUnpacker unpacker) throws IOException {
    username = unpacker.unpackString();
  }
}
