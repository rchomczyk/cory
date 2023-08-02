package moe.rafal.cory.packet.message;

public final class PacketListenerRegistryFactory {

  private PacketListenerRegistryFactory() {

  }

  public static PacketListenerRegistry producePacketListenerRegistry() {
    return new MapBasedPacketListenerRegistry();
  }
}
