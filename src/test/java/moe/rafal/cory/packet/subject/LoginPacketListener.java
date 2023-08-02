package moe.rafal.cory.packet.subject;

import moe.rafal.cory.packet.message.PacketListener;

public class LoginPacketListener implements PacketListener<LoginPacket> {

  @Override
  public void receive(String channelName, LoginPacket packet) {
    System.out.printf("User %s attempted to login with password %s%n",
        packet.getUsername(),
        packet.getPassword());
  }

  @Override
  public Class<LoginPacket> getPacketType() {
    return LoginPacket.class;
  }
}
