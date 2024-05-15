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

import static java.lang.String.format;

import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class MessagePackPacketPackerUtils {

  static final Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED_TYPE;
  static final Map<Class<?>, ThrowingBiConsumer<PacketPacker, ?, IOException>>
      PACKET_PACKER_BY_BOXED_TYPE;
  static final Map<Class<?>, ThrowingFunction<PacketUnpacker, ?, IOException>>
      PACKET_UNPACKER_BY_BOXED_TYPE;

  static {
    PRIMITIVE_TO_BOXED_TYPE = new HashMap<>();
    PRIMITIVE_TO_BOXED_TYPE.put(boolean.class, Boolean.class);
    PRIMITIVE_TO_BOXED_TYPE.put(int.class, Integer.class);
    PRIMITIVE_TO_BOXED_TYPE.put(byte.class, Byte.class);
    PRIMITIVE_TO_BOXED_TYPE.put(long.class, Long.class);
    PRIMITIVE_TO_BOXED_TYPE.put(short.class, Short.class);
    PRIMITIVE_TO_BOXED_TYPE.put(float.class, Float.class);
    PRIMITIVE_TO_BOXED_TYPE.put(double.class, Double.class);
    PACKET_PACKER_BY_BOXED_TYPE = new HashMap<>();
    PACKET_PACKER_BY_BOXED_TYPE.put(
        String.class, (packer, value) -> packer.packString((String) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Boolean.class, (packer, value) -> packer.packBoolean((Boolean) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Integer.class, (packer, value) -> packer.packInt((Integer) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(Byte.class, (packer, value) -> packer.packByte((Byte) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(Long.class, (packer, value) -> packer.packLong((Long) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(UUID.class, (packer, value) -> packer.packUUID((UUID) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Short.class, (packer, value) -> packer.packShort((Short) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Float.class, (packer, value) -> packer.packFloat((Float) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Double.class, (packer, value) -> packer.packDouble((Double) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Instant.class, (packer, value) -> packer.packInstant((Instant) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Duration.class, (packer, value) -> packer.packDuration((Duration) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Enum.class, (packer, value) -> packer.packEnum((Enum<?>) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Array.class, (packer, value) -> packer.packArray((Object[]) value));
    PACKET_PACKER_BY_BOXED_TYPE.put(
        Map.class, (packer, value) -> packer.packMap((Map<?, ?>) value));
    PACKET_UNPACKER_BY_BOXED_TYPE = new HashMap<>();
    PACKET_UNPACKER_BY_BOXED_TYPE.put(String.class, PacketUnpacker::unpackString);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Boolean.class, PacketUnpacker::unpackBoolean);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Integer.class, PacketUnpacker::unpackInt);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Byte.class, PacketUnpacker::unpackByte);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Long.class, PacketUnpacker::unpackLong);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(UUID.class, PacketUnpacker::unpackUUID);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Short.class, PacketUnpacker::unpackShort);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Float.class, PacketUnpacker::unpackFloat);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Double.class, PacketUnpacker::unpackDouble);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Instant.class, PacketUnpacker::unpackInstant);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Duration.class, PacketUnpacker::unpackDuration);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Enum.class, PacketUnpacker::unpackEnum);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Array.class, PacketUnpacker::unpackArray);
    PACKET_UNPACKER_BY_BOXED_TYPE.put(Map.class, PacketUnpacker::unpackMap);
  }

  private MessagePackPacketPackerUtils() {}

  static Class<?> getClassByNameOrThrow(final String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException exception) {
      throw new PacketUnpackingException(format("Could not find class by name %s", className));
    }
  }

  static Class<?> getBoxedType(final Class<?> type) {
    if (type.isPrimitive()) {
      return PRIMITIVE_TO_BOXED_TYPE.get(type);
    }

    if (type.isEnum()) {
      return Enum.class;
    }

    if (type.isArray()) {
      return Array.class;
    }

    if (Map.class.isAssignableFrom(type)) {
      return Map.class;
    }

    return type;
  }
}
