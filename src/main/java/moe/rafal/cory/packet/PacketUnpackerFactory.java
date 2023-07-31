package moe.rafal.cory.packet;

import org.msgpack.core.MessagePack;

public final class PacketUnpackerFactory {

  private PacketUnpackerFactory() {

  }

  public static PacketUnpacker producePacketUnpacker(byte[] content) {
    return new MessagePackPacketUnpacker(MessagePack.newDefaultUnpacker(content));
  }
}
