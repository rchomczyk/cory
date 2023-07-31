package moe.rafal.cory.packet;

import java.io.IOException;
import org.msgpack.core.MessageUnpacker;

class MessagePackPacketUnpacker implements PacketUnpacker {

  private final MessageUnpacker underlyingUnpacker;

  MessagePackPacketUnpacker(MessageUnpacker underlyingUnpacker) {
    this.underlyingUnpacker = underlyingUnpacker;
  }

  @Override
  public void skipValue() throws IOException {
    underlyingUnpacker.skipValue();
  }

  @Override
  public int unpackArrayHeader() throws IOException {
    return underlyingUnpacker.unpackArrayHeader();
  }

  @Override
  public int unpackBinaryHeader() throws IOException {
    return underlyingUnpacker.unpackBinaryHeader();
  }

  @Override
  public String unpackString() throws IOException {
    return underlyingUnpacker.unpackString();
  }

  @Override
  public boolean unpackBoolean() throws IOException {
    return underlyingUnpacker.unpackBoolean();
  }

  @Override
  public int unpackInt() throws IOException {
    return underlyingUnpacker.unpackInt();
  }

  @Override
  public byte unpackByte() throws IOException {
    return underlyingUnpacker.unpackByte();
  }

  @Override
  public long unpackLong() throws IOException {
    return underlyingUnpacker.unpackLong();
  }

  @Override
  public short unpackShort() throws IOException {
    return underlyingUnpacker.unpackShort();
  }

  @Override
  public float unpackFloat() throws IOException {
    return underlyingUnpacker.unpackFloat();
  }

  @Override
  public double unpackDouble() throws IOException {
    return underlyingUnpacker.unpackDouble();
  }

  @Override
  public int unpackMapHeader() throws IOException {
    return underlyingUnpacker.unpackMapHeader();
  }

  @Override
  public void close() throws IOException {
    underlyingUnpacker.close();
  }
}
