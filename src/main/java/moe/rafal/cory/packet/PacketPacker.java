package moe.rafal.cory.packet;

import java.util.Map;

public interface PacketPacker {

  <T> void packArray(T[] value);

  void packBinaryArray(byte[] value);

  void packString(String value);

  void packBoolean(boolean value);

  void packInt(int value);

  void packByte(byte value);

  void packLong(long value);

  void packShort(short value);

  void packFloat(float value);

  void packDouble(double value);

  <K, V> void packMap(Map<K, V> value);
}
