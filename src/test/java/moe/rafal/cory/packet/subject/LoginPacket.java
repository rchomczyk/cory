package moe.rafal.cory.packet.subject;

import java.io.IOException;
import moe.rafal.cory.packet.Packet;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

public class LoginPacket extends Packet {

  private String username;
  private String password;

  public LoginPacket(String username, String password) {
    super();
    this.username = username;
    this.password = password;
  }

  public LoginPacket() {
    super();
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