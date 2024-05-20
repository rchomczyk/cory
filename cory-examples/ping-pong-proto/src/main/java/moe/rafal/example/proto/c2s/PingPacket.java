package moe.rafal.example.proto.c2s;

import java.io.IOException;
import moe.rafal.cory.Packet;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;

public class PingPacket extends Packet {

  private String message;

  public PingPacket() {}

  @Override
  public void write(final PacketPacker packer) throws IOException {
    packer.packString(message);
  }

  @Override
  public void read(final PacketUnpacker unpacker) throws IOException {
    message = unpacker.unpackString();
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
