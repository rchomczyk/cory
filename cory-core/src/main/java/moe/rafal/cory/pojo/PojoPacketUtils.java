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

package moe.rafal.cory.pojo;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;

final class PojoPacketUtils {

  private PojoPacketUtils() {}

  static Field getDeclaredFieldByNameOrNull(final String name, final Class<?> clazz) {
    try {
      return clazz.getDeclaredField(name);
    } catch (final NoSuchFieldException exception) {
      return null;
    }
  }

  static void writePojo(final PacketPacker packer, final Object value) throws IOException {
    final Class<?> type = value.getClass();
    try {
      final List<Field> fields =
          Arrays.stream(type.getDeclaredFields())
              .filter(not(field -> isTransient(field.getModifiers())))
              .collect(toUnmodifiableList());

      packer.packInt(fields.size());
      for (final Field field : fields) {
        writeField(packer, field, value);
      }
    } catch (final IllegalAccessException exception) {
      throw new PojoWritingException(
          format("Could not write pojo with name %s", type.getName()), exception);
    }
  }

  private static void writeField(final PacketPacker packer, final Field field, final Object value)
      throws IllegalAccessException, IOException {
    field.setAccessible(true);
    packer.packString(field.getName());
    packer.packAuto(field.get(value));
  }

  static void parsePojo(final PacketUnpacker unpacker, final Object value) throws IOException {
    final Class<?> type = value.getClass();
    try {
      final int fieldCount = unpacker.unpackInt();
      for (int index = 0; index < fieldCount; index++) {
        parseField(unpacker, value);
      }
    } catch (final IllegalAccessException exception) {
      throw new PojoParsingException(
          format("Could not parse pojo with name %s", type.getName()), exception);
    }
  }

  private static void parseField(final PacketUnpacker unpacker, final Object value)
      throws IllegalAccessException, IOException {
    final Class<?> type = value.getClass();

    final String fieldName = unpacker.unpackString();
    final Field field = getDeclaredFieldByNameOrNull(fieldName, type);
    if (field == null) {
      throw new PojoParsingException(format("Could not find field with name %s", fieldName));
    }

    field.setAccessible(true);
    field.set(value, unpacker.unpackAuto());
  }
}
