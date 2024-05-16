package moe.rafal.example;

import moe.rafal.cory.message.packet.PacketListenerDelegate;
import moe.rafal.example.proto.c2s.PingPacket;
import moe.rafal.example.proto.s2c.PongPacket;

public class PongListener extends PacketListenerDelegate<PingPacket> {

  protected PongListener() {
    super(PingPacket.class, true);
  }

  private static PongPacket getPongPacket(final PingPacket request) {
    PongPacket pongPacket = new PongPacket();
    pongPacket.setMessage(request.getMessage() + " Pong!");
    return pongPacket;
  }

  @Override
  public PongPacket process(
      final String channelName, final String replyChannelName, final PingPacket packet) {
    return getPongPacket(packet);
  }
}
