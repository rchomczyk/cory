package moe.rafal.cory.packet;

public final class PacketGatewayFactory {

  private PacketGatewayFactory() {

  }

  public static PacketGateway producePacketGateway() {
    return new PacketGatewayImpl();
  }
}
