package moe.rafal.cory.packet;

import java.io.IOException;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

class MessagePackPacket extends Packet {

  private String username;
  private String password;

  MessagePackPacket(String username, String password) {
    super();
    this.username = username;
    this.password = password;
  }

  @Override
  public void write(PacketPacker packer) throws IOException {
    packer.packString(username);
    packer.packString(password);
  }

  @Override
  public void read(PacketUnpacker unpacker) throws IOException {
    username = unpacker.unpackString();
    password = unpacker.unpackString();
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
