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

import static moe.rafal.cory.serdes.MessagePackPacketPackerUtils.PACKET_UNPACKER_BY_BOXED_TYPE;
import static moe.rafal.cory.serdes.MessagePackPacketPackerUtils.getClassByNameOrThrow;
import static org.msgpack.core.MessageFormat.NIL;

import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
  public @SuppressWarnings("unchecked") <V> V[] unpackArray() throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    final String className = underlyingUnpacker.unpackString();
    final Class<?> type = getClassByNameOrThrow(className);
    return unpackOrNil(unpacker -> {
      final int length = unpacker.unpackArrayHeader();
      final V[] result = (V[]) Array.newInstance(type, length);
      for (int index = 0; index < length; index++) {
        result[index] = unpackAuto();
      }

      return result;
    });
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
    return unpackOrNil(MessageUnpacker::unpackString);
  }

  @Override
  public Boolean unpackBoolean() throws IOException {
    return unpackOrNil(MessageUnpacker::unpackBoolean);
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
  public <K, V> Map<K, V> unpackMap()
      throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    final int length = unpackMapHeader();

    final Map<K, V> result = new HashMap<>(length);
    for (int index = 0; index < length; index++) {
      result.put(
          unpackAuto(),
          unpackAuto()
      );
    }

    return result;
  }

  @Override
  public Instant unpackInstant() throws IOException {
    return unpackOrNil(unpacker -> Instant.parse(unpacker.unpackString()));
  }

  @Override
  public Duration unpackDuration() throws IOException {
    return unpackOrNil(unpacker -> Duration.ofMillis(unpacker.unpackLong()));
  }

  @Override
  public @SuppressWarnings("unchecked") <T extends Enum<T>> T unpackEnum() throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    final String className = unpackString();
    final Class<?> type = getClassByNameOrThrow(className);
    if (type.isEnum()) {
      return unpackOrNil(unpacker -> Enum.valueOf((Class<T>) type, unpacker.unpackString()));
    }

    return null;
  }

  @Override
  public @SuppressWarnings("unchecked") <T> T unpackAuto() throws IOException {
    if (hasNextNilValue()) {
      return null;
    }

    final String className = underlyingUnpacker.unpackString();
    final Class<?> type = getClassByNameOrThrow(className);
    final ThrowingFunction<PacketUnpacker, T, IOException> unpackerFunction =
        (ThrowingFunction<PacketUnpacker, T, IOException>) PACKET_UNPACKER_BY_BOXED_TYPE.get(
            type.isEnum() ? Enum.class : type
        );
    return unpackerFunction.apply(this);
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
