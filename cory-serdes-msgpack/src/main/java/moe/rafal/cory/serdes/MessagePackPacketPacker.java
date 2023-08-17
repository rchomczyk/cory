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

import com.pivovarit.function.ThrowingBiConsumer;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePacker;

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
  public PacketPacker packPayload(byte[] value) throws IOException {
    underlyingPacker.writePayload(value);
    return this;
  }

  @Override
  public PacketPacker packString(String value) throws IOException {
    return packOrNil(value, MessagePacker::packString);
  }

  @Override
  public PacketPacker packBoolean(Boolean value) throws IOException {
    return packOrNil(value, MessagePacker::packBoolean);
  }

  @Override
  public PacketPacker packInt(Integer value) throws IOException {
    return packOrNil(value, MessagePacker::packInt);
  }

  @Override
  public PacketPacker packByte(Byte value) throws IOException {
    return packOrNil(value, MessagePacker::packByte);
  }

  @Override
  public PacketPacker packLong(Long value) throws IOException {
    return packOrNil(value, MessagePacker::packLong);
  }

  @Override
  public PacketPacker packUUID(UUID value) throws IOException {
    return packOrNil(value, (packer, ignored) -> {
      packer.packLong(value.getMostSignificantBits());
      packer.packLong(value.getLeastSignificantBits());
    });
  }

  @Override
  public PacketPacker packShort(Short value) throws IOException {
    return packOrNil(value, MessagePacker::packShort);
  }

  @Override
  public PacketPacker packFloat(Float value) throws IOException {
    return packOrNil(value, MessagePacker::packFloat);
  }

  @Override
  public PacketPacker packDouble(Double value) throws IOException {
    return packOrNil(value, MessagePacker::packDouble);
  }

  @Override
  public PacketPacker packMapHeader(int value) throws IOException {
    underlyingPacker.packMapHeader(value);
    return this;
  }

  @Override
  public PacketPacker packInstant(Instant value) throws IOException {
    return value == null ? packNil() : packString(value.toString());
  }

  @Override
  public PacketPacker packDuration(Duration value) throws IOException {
    return value == null ? packNil() : packString(value.toString());
  }

  @Override
  public PacketPacker packEnum(Enum<?> value) throws IOException {
    return value == null ? packNil() : packString(value.name());
  }

  @Override
  public PacketPacker packNil() throws IOException {
    underlyingPacker.packNil();
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

  private <T> PacketPacker packOrNil(T value,
      ThrowingBiConsumer<MessagePacker, T, IOException> packFunction) throws IOException {
    if (value == null) {
      underlyingPacker.packNil();
      return this;
    }

    packFunction.accept(underlyingPacker, value);
    return this;
  }
}
