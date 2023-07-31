package moe.rafal.cory.packet;

import java.util.Map;

public interface PacketPacker {

  <T> PacketPacker packArray(T[] value);

  PacketPacker packBinaryArray(byte[] value);

  PacketPacker packString(String value);

  PacketPacker packBoolean(boolean value);

  PacketPacker packInt(int value);

  PacketPacker packByte(byte value);

  PacketPacker packLong(long value);

  PacketPacker packShort(short value);

  PacketPacker packFloat(float value);

  PacketPacker packDouble(double value);

  <K, V> PacketPacker packMap(Map<K, V> value);

  byte[] toBinaryArray();
}
