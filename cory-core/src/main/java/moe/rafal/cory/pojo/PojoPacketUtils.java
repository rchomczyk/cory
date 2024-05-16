/*
 *    Copyright 2023-2024 cory
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
import static moe.rafal.cory.pojo.ReflectionUtils.getDeclaredFields;
import static moe.rafal.cory.pojo.ReflectionUtils.getFieldValue;
import static moe.rafal.cory.pojo.ReflectionUtils.setFieldValue;

import java.io.IOException;
import java.lang.reflect.Field;
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

  static void writePojo(final PacketPacker packer, final Object instance) throws IOException {
    final Field[] fields = getDeclaredFields(instance.getClass());
    packer.packInt(fields.length);
    for (final Field field : fields) {
      writeField(packer, field, instance);
    }
  }

  static void parsePojo(final PacketUnpacker unpacker, final Object value) throws IOException {
    final int fieldCount = unpacker.unpackInt();
    for (int index = 0; index < fieldCount; index++) {
      parseField(unpacker, value);
    }
  }

  private static void writeField(
      final PacketPacker packer, final Field field, final Object instance) throws IOException {
    field.setAccessible(true);
    packer.packString(field.getName());
    packer.packAuto(getFieldValue(field, instance));
  }

  private static void parseField(final PacketUnpacker unpacker, final Object instance)
      throws IOException {
    final String fieldName = unpacker.unpackString();
    final Field field = getDeclaredFieldByNameOrNull(fieldName, instance.getClass());
    if (field == null) {
      throw new PojoParsingException(format("Could not find field with name %s", fieldName));
    }
    setFieldValue(field, instance, unpacker.unpackAuto());
  }
}
