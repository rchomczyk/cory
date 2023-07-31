package moe.rafal.cory.packet;

import java.util.Map;

public interface PacketUnpacker {

  void skipValue();

  <T> T[] unpackArray(Class<T> arrayType);

  byte[] unpackBinaryArray();

  String unpackString();

  boolean unpackBoolean();

  int unpackInt();

  byte unpackByte();

  long unpackLong();

  short unpackShort();

  float unpackFloat();

  double unpackDouble();

  <K, V> Map<K, V> unpackMap(Class<K> keyType, Class<V> valueType);
}
