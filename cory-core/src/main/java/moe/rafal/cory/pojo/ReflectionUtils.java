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
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

final class ReflectionUtils {

  private static final Map<Class<?>, Field[]> FIELD_CACHE = new HashMap<>();
  private static final Map<Field, MethodHandle> FIELD_GETTERS_CACHE = new HashMap<>();
  private static final Map<Field, MethodHandle> FIELD_SETTERS_CACHE = new HashMap<>();
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  private ReflectionUtils() {}

  static Object getFieldValue(final Field field, final Object instance) {
    field.setAccessible(true);
    try {
      return getGetter(field).invoke(instance);
    } catch (final Throwable exception) {
      throw new PojoWritingException(
          format("Could not get field value for field with name %s", field.getName()), exception);
    }
  }

  static void setFieldValue(final Field field, final Object instance, final Object value) {
    field.setAccessible(true);
    try {
      getSetter(field).invoke(instance, value);
    } catch (final Throwable exception) {
      throw new PojoWritingException(
          format("Could not set field value for field with name %s", field.getName()), exception);
    }
  }

  private static MethodHandle getGetter(final Field field) {
    return FIELD_GETTERS_CACHE.computeIfAbsent(field, ReflectionUtils::getGetter0);
  }

  private static MethodHandle getGetter0(final Field field) {
    try {
      return LOOKUP.unreflectGetter(field);
    } catch (final IllegalAccessException exception) {
      throw new PojoWritingException(
          format("Could not get getter for field with name %s", field.getName()), exception);
    }
  }

  private static MethodHandle getSetter(final Field field) {
    return FIELD_SETTERS_CACHE.computeIfAbsent(field, ReflectionUtils::getSetter0);
  }

  private static MethodHandle getSetter0(final Field field) {
    try {
      return LOOKUP.unreflectSetter(field);
    } catch (final IllegalAccessException exception) {
      throw new PojoWritingException(
          format("Could not get setter for field with name %s", field.getName()), exception);
    }
  }

  static Field[] getDeclaredFields(final Class<?> clazz) {
    return FIELD_CACHE.computeIfAbsent(clazz, ReflectionUtils::getDeclaredFields0);
  }

  private static Field[] getDeclaredFields0(final Class<?> clazz) {
    return stream(clazz.getDeclaredFields())
        .filter(not(field -> isTransient(field.getModifiers())))
        .toArray(Field[]::new);
  }
}
