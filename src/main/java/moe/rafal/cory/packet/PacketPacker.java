package moe.rafal.cory.packet;

import java.io.Closeable;
import java.io.IOException;

public interface PacketPacker extends Closeable {

  PacketPacker packArrayHeader(int value) throws IOException;

  PacketPacker packBinaryHeader(int value) throws IOException;

  PacketPacker packString(String value) throws IOException;

  PacketPacker packBoolean(boolean value) throws IOException;

  PacketPacker packInt(int value) throws IOException;

  PacketPacker packByte(byte value) throws IOException;

  PacketPacker packLong(long value) throws IOException;

  PacketPacker packShort(short value) throws IOException;

  PacketPacker packFloat(float value) throws IOException;

  PacketPacker packDouble(double value) throws IOException;

  PacketPacker packMapHeader(int value) throws IOException;

  PacketPacker flush() throws IOException;

  byte[] toBinaryArray();
}
