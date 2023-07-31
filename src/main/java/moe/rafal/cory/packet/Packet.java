package moe.rafal.cory.packet;

import java.util.UUID;

public abstract class Packet {

  private final UUID uniqueId;

  protected Packet(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  protected Packet() {
    this(UUID.randomUUID());
  }

  public abstract void read(PacketUnpacker unpacker);

  public UUID getUniqueId() {
    return uniqueId;
  }
}
