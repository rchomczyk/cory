package moe.rafal.cory.packet;

public interface PacketGateway extends PacketWriter, PacketReader {

  PacketGateway INSTANCE = new PacketGatewayImpl();
}
