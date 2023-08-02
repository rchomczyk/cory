package moe.rafal.cory.packet;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

public abstract class Packet {

  private UUID uniqueId;

  protected Packet(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  protected Packet() {
    this(UUID.randomUUID());
  }

  public abstract void write(PacketPacker packer) throws IOException;

  public abstract void read(PacketUnpacker unpacker) throws IOException;

  public UUID getUniqueId() {
    return uniqueId;
  }

  protected void setUniqueId(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public boolean equals(Object comparedObject) {
    if (this == comparedObject) {
      return true;
    }

    if (comparedObject == null || getClass() != comparedObject.getClass()) {
      return false;
    }

    Packet packet = (Packet) comparedObject;
    return Objects.equals(uniqueId, packet.uniqueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uniqueId);
  }
}
