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

import static moe.rafal.cory.MessagePackAssertions.getBinaryArrayOf;
import static moe.rafal.cory.MessagePackAssertions.unpackValueAndAssertThatEqualTo;
import static moe.rafal.cory.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MessagePackPacketUnpackerTests {

  private static final int DEFAULT_VALUE = 0;

  @AfterEach
  void closePacketUnpacker() {
    assertThatCode(() -> producePacketUnpacker(new byte[0]).close())
        .doesNotThrowAnyException();
  }

  @Test
  void skipValueTest() throws IOException {
    byte[] content = getBinaryArrayOf((packer, expectedValue) -> {
      packer.packString("test_string_1");
      packer.packString("test_string_2");
      packer.packString("test_string_3");
    }, DEFAULT_VALUE);
    try (PacketUnpacker unpacker = producePacketUnpacker(content)) {
      assertThat(unpacker.unpackString())
          .isEqualTo("test_string_1");
      unpacker.skipValue();
      assertThat(unpacker.unpackString())
          .isEqualTo("test_string_3");
    }
  }

  @ValueSource(ints = {10, 20, 30})
  @ParameterizedTest
  void unpackArrayHeaderTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packArrayHeader,
        PacketUnpacker::unpackArrayHeader, value);
  }

  @ValueSource(ints = {40, 50, 60})
  @ParameterizedTest
  void unpackBinaryHeaderTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packBinaryHeader,
        PacketUnpacker::unpackBinaryHeader, value);
  }

  @ValueSource(strings = {"test_string_1", "test_string_2", "test_string_3"})
  @ParameterizedTest
  void unpackStringTest(String value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packString,
        PacketUnpacker::unpackString, value);
  }

  @ValueSource(booleans = {true, false})
  @ParameterizedTest
  void unpackBooleanTest(boolean value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packBoolean,
        PacketUnpacker::unpackBoolean, value);
  }

  @ValueSource(ints = {600, 700, 800})
  @ParameterizedTest
  void unpackIntTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packInt,
        PacketUnpacker::unpackInt, value);
  }

  @ValueSource(bytes = {0, 1})
  @ParameterizedTest
  void unpackByteTest(byte value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packByte,
        PacketUnpacker::unpackByte, value);
  }

  @ValueSource(longs = {100000L, 200000000L, 300000000000L})
  @ParameterizedTest
  void unpackLongTest(long value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packLong,
        PacketUnpacker::unpackLong, value);
  }

  @MethodSource("getUuidSubjects")
  @ParameterizedTest
  void unpackUUIDTest(UUID value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packUUID,
        PacketUnpacker::unpackUUID, value);
  }

  private static Set<UUID> getUuidSubjects() {
    return Set.of(
        UUID.nameUUIDFromBytes("test_subject_1".getBytes(StandardCharsets.UTF_8)),
        UUID.nameUUIDFromBytes("test_subject_2".getBytes(StandardCharsets.UTF_8)),
        UUID.nameUUIDFromBytes("test_subject_3".getBytes(StandardCharsets.UTF_8)));
  }

  @ValueSource(shorts = {10, 30, 4})
  @ParameterizedTest
  void unpackShortTest(short value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packShort,
        PacketUnpacker::unpackShort, value);
  }

  @ValueSource(floats = {1.3F, 2.3F, 5.0F})
  @ParameterizedTest
  void unpackFloatTest(float value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packFloat,
        PacketUnpacker::unpackFloat, value);
  }

  @ValueSource(doubles = {100.50, 30000.3131, 50000.00})
  @ParameterizedTest
  void unpackDoubleTest(double value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packDouble,
        PacketUnpacker::unpackDouble, value);
  }

  @ValueSource(ints = {1, 2, 3})
  @ParameterizedTest
  void unpackMapHeaderTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packInt,
        PacketUnpacker::unpackInt, value);
  }

  @MethodSource("getInstantSubjects")
  @ParameterizedTest
  void unpackInstantTest(Instant value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packInstant,
        PacketUnpacker::unpackInstant, value);
  }

  private static Set<Instant> getInstantSubjects() {
    return Set.of(
        Instant.parse("2023-08-01T12:00:00.00Z"),
        Instant.parse("2023-08-02T12:00:00.00Z"),
        Instant.parse("2023-08-03T12:00:00.00Z"));
  }

  @MethodSource("getDurationSubjects")
  @ParameterizedTest
  void unpackDurationTest() throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packDuration,
        PacketUnpacker::unpackDuration, Duration.ofSeconds(30));
  }

  private static Set<Duration> getDurationSubjects() {
    return Set.of(Duration.ofSeconds(30), Duration.ofHours(2), Duration.ofDays(1));
  }

  @Test
  void hasNextOnEmptyUnpackerTest() throws IOException {
    try (PacketUnpacker unpacker = producePacketUnpacker(new byte[0])) {
      assertThat(unpacker.hasNext())
          .isFalse();
    }
  }

  @Test
  void hasNextOnExhaustedUnpackerTest() throws IOException {
    try (PacketUnpacker unpacker = producePacketUnpacker(
        getBinaryArrayOf(PacketPacker::packInt, 1))) {
      assertThat(unpacker.hasNext())
          .isTrue();
    }
  }
}
