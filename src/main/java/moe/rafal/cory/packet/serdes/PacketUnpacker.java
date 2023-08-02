package moe.rafal.cory.packet.serdes;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

public interface PacketUnpacker extends Closeable {

  void skipValue() throws IOException;

  int unpackArrayHeader() throws IOException;

  int unpackBinaryHeader() throws IOException;

  String unpackString() throws IOException;

  boolean unpackBoolean() throws IOException;

  int unpackInt() throws IOException;

  byte unpackByte() throws IOException;

  long unpackLong() throws IOException;

  UUID unpackUUID() throws IOException;

  short unpackShort() throws IOException;

  float unpackFloat() throws IOException;

  double unpackDouble() throws IOException;

  int unpackMapHeader() throws IOException;
}
