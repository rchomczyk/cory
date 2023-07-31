package moe.rafal.cory.packet;

import org.msgpack.core.MessagePack;

public final class PacketPackerFactory {

  private PacketPackerFactory() {

  }

  public static PacketPacker producePacketPacker() {
    return new MessagePackPacketPacker(MessagePack.newDefaultBufferPacker());
  }
}
