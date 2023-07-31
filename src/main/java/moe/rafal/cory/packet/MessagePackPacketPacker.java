package moe.rafal.cory.packet;

import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

class MessagePackPacketPacker implements PacketPacker {

  private final MessageBufferPacker underlyingPacker;

  MessagePackPacketPacker(MessageBufferPacker underlyingPacker) {
    this.underlyingPacker = underlyingPacker;
  }

  @Override
  public PacketPacker packArrayHeader(int value) throws IOException {
    underlyingPacker.packArrayHeader(value);
    return this;
  }

  @Override
  public PacketPacker packBinaryHeader(int value) throws IOException {
    underlyingPacker.packBinaryHeader(value);
    return this;
  }

  @Override
  public PacketPacker packString(String value) throws IOException {
    underlyingPacker.packString(value);
    return this;
  }

  @Override
  public PacketPacker packBoolean(boolean value) throws IOException {
    underlyingPacker.packBoolean(value);
    return this;
  }

  @Override
  public PacketPacker packInt(int value) throws IOException {
    underlyingPacker.packInt(value);
    return this;
  }

  @Override
  public PacketPacker packByte(byte value) throws IOException {
    underlyingPacker.packByte(value);
    return this;
  }

  @Override
  public PacketPacker packLong(long value) throws IOException {
    underlyingPacker.packLong(value);
    return this;
  }

  @Override
  public PacketPacker packShort(short value) throws IOException {
    underlyingPacker.packShort(value);
    return this;
  }

  @Override
  public PacketPacker packFloat(float value) throws IOException {
    underlyingPacker.packFloat(value);
    return this;
  }

  @Override
  public PacketPacker packDouble(double value) throws IOException {
    underlyingPacker.packDouble(value);
    return this;
  }

  @Override
  public PacketPacker packMapHeader(int value) throws IOException {
    underlyingPacker.packMapHeader(value);
    return this;
  }

  @Override
  public byte[] toBinaryArray() {
    return underlyingPacker.toByteArray();
  }

  @Override
  public PacketPacker flush() throws IOException {
    underlyingPacker.flush();
    return this;
  }

  @Override
  public void close() throws IOException {
    underlyingPacker.close();
  }
}
