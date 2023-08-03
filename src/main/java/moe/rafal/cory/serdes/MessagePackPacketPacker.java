/*
 *    Copyright 2023 cory
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package moe.rafal.cory.serdes;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
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
  public PacketPacker packUUID(UUID value) throws IOException {
    packMappedValueOrNil(value, UUID::toString);
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
  public PacketPacker packInstant(Instant value) throws IOException {
    packMappedValueOrNil(value, Instant::toString);
    return this;
  }

  @Override
  public PacketPacker packDuration(Duration value) throws IOException {
    packMappedValueOrNil(value, Duration::toString);
    return this;
  }

  private <T> void packMappedValueOrNil(T value, Function<T, String> valueMapper)
      throws IOException {
    if (value == null) {
      underlyingPacker.packNil();
      return;
    }

    underlyingPacker.packString(valueMapper.apply(value));
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
