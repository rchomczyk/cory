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

  public abstract void write(PacketPacker packer);

  public UUID getUniqueId() {
    return uniqueId;
  }
}
