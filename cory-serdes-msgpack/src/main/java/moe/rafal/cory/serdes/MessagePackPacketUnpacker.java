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

import static org.msgpack.core.MessageFormat.NIL;

import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
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
  public byte[] unpackPayload() throws IOException {
    return underlyingUnpacker.readPayload(unpackBinaryHeader());
  }

  @Override
  public String unpackString() throws IOException {
    return underlyingUnpacker.unpackString();
  }

  @Override
  public Boolean unpackBoolean() throws IOException {
    return underlyingUnpacker.unpackBoolean();
  }

  @Override
  public Integer unpackInt() throws IOException {
    return unpackOrNil(MessageUnpacker::unpackInt);
  }

  @Override
  public Byte unpackByte() throws IOException {
    return unpackOrNil(MessageUnpacker::unpackByte);
  }

  @Override
  public Long unpackLong() throws IOException {
    return unpackOrNil(MessageUnpacker::unpackLong);
  }

  @Override
  public UUID unpackUUID() throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    return new UUID(
        underlyingUnpacker.unpackLong(),
        underlyingUnpacker.unpackLong());
  }

  @Override
  public Short unpackShort() throws IOException {
    return unpackOrNil(MessageUnpacker::unpackShort);
  }

  @Override
  public Float unpackFloat() throws IOException {
    return unpackOrNil(MessageUnpacker::unpackFloat);
  }

  @Override
  public Double unpackDouble() throws IOException {
    return unpackOrNil(MessageUnpacker::unpackDouble);
  }

  @Override
  public int unpackMapHeader() throws IOException {
    return underlyingUnpacker.unpackMapHeader();
  }

  @Override
  public Instant unpackInstant() throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    return Instant.parse(underlyingUnpacker.unpackString());
  }

  @Override
  public Duration unpackDuration() throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    return Duration.parse(underlyingUnpacker.unpackString());
  }

  @Override
  public <T extends Enum<T>> T unpackEnum(Class<T> expectedType) throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    return Enum.valueOf(expectedType, underlyingUnpacker.unpackString());
  }

  @Override
  public boolean hasNext() throws IOException {
    return underlyingUnpacker.hasNext();
  }

  @Override
  public boolean hasNextNilValue() throws IOException {
    return hasNext() && underlyingUnpacker.getNextFormat() == NIL;
  }

  @Override
  public void close() throws IOException {
    underlyingUnpacker.close();
  }

  private <T> T unpackOrNil(ThrowingFunction<MessageUnpacker, T, IOException> unpackFunction)
      throws IOException {
    return hasNextNilValue() ? null : unpackFunction.apply(underlyingUnpacker);
  }
}
