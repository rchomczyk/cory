package moe.rafal.cory.packet;

import java.util.UUID;

public class Packet {

  private final UUID uniqueId;

  protected Packet(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  protected Packet() {
    this(UUID.randomUUID());
  }

  public UUID getUniqueId() {
    return uniqueId;
  }
}
