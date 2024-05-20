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

package moe.rafal.cory.serdes;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofSeconds;
import static moe.rafal.cory.MessagePackAssertions.getBinaryArrayOf;
import static moe.rafal.cory.MessagePackAssertions.unpackValueAndAssertThatEqualTo;
import static moe.rafal.cory.serdes.MessagePackPacketSerdesContext.getMessagePackPacketSerdesContext;
import static moe.rafal.cory.subject.GameState.AWAITING;
import static moe.rafal.cory.subject.GameState.COUNTING;
import static moe.rafal.cory.subject.GameState.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import moe.rafal.cory.subject.GameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MessagePackPacketUnpackerTests {

  private static final int DEFAULT_VALUE = 0;

  private static Set<UUID> getUuidSubjects() {
    return Set.of(
        UUID.nameUUIDFromBytes("test_subject_1".getBytes(StandardCharsets.UTF_8)),
        UUID.nameUUIDFromBytes("test_subject_2".getBytes(StandardCharsets.UTF_8)),
        UUID.nameUUIDFromBytes("test_subject_3".getBytes(StandardCharsets.UTF_8)));
  }

  private static Set<Instant> getInstantSubjects() {
    return Set.of(
        Instant.parse("2023-08-01T12:00:00.00Z"),
        Instant.parse("2023-08-02T12:00:00.00Z"),
        Instant.parse("2023-08-03T12:00:00.00Z"));
  }

  private static Set<Duration> getDurationSubjects() {
    return Set.of(ofSeconds(30), ofHours(2), ofDays(1));
  }

  private static Set<GameState> getEnumSubjects() {
    return Set.of(AWAITING, COUNTING, RUNNING);
  }

  @AfterEach
  void closePacketUnpacker() {
    assertThatCode(
            () -> getMessagePackPacketSerdesContext().newPacketUnpacker(new byte[0]).close())
        .doesNotThrowAnyException();
  }

  @Test
  void skipValueTest() throws IOException {
    byte[] content =
        getBinaryArrayOf(
            (packer, expectedValue) -> {
              packer.packString("test_string_1");
              packer.packString("test_string_2");
              packer.packString("test_string_3");
            },
            DEFAULT_VALUE);
    try (PacketUnpacker unpacker =
        getMessagePackPacketSerdesContext().newPacketUnpacker(content)) {
      assertThat(unpacker.unpackString()).isEqualTo("test_string_1");
      unpacker.skipValue();
      assertThat(unpacker.unpackString()).isEqualTo("test_string_3");
    }
  }

  @ValueSource(ints = {10, 20, 30})
  @ParameterizedTest
  void unpackArrayHeaderTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packArrayHeader, PacketUnpacker::unpackArrayHeader, value);
  }

  @Test
  void unpackArrayTest() throws IOException {
    final String[] value = new String[] {"test_string_1", "test_string_2", "test_string_3"};
    unpackValueAndAssertThatEqualTo(PacketPacker::packArray, PacketUnpacker::unpackArray, value);
  }

  @ValueSource(ints = {40, 50, 60})
  @ParameterizedTest
  void unpackBinaryHeaderTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packBinaryHeader, PacketUnpacker::unpackBinaryHeader, value);
  }

  @ValueSource(strings = {"test_string_1", "test_string_2", "test_string_3"})
  @ParameterizedTest
  void unpackStringTest(String value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packString, PacketUnpacker::unpackString, value);
  }

  @ValueSource(booleans = {true, false})
  @ParameterizedTest
  void unpackBooleanTest(boolean value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packBoolean, PacketUnpacker::unpackBoolean, value);
  }

  @ValueSource(ints = {600, 700, 800})
  @ParameterizedTest
  void unpackIntTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packInt, PacketUnpacker::unpackInt, value);
  }

  @ValueSource(bytes = {0, 1})
  @ParameterizedTest
  void unpackByteTest(byte value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packByte, PacketUnpacker::unpackByte, value);
  }

  @ValueSource(longs = {100000L, 200000000L, 300000000000L})
  @ParameterizedTest
  void unpackLongTest(long value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packLong, PacketUnpacker::unpackLong, value);
  }

  @MethodSource("getUuidSubjects")
  @ParameterizedTest
  void unpackUUIDTest(UUID value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packUUID, PacketUnpacker::unpackUUID, value);
  }

  @ValueSource(shorts = {10, 30, 4})
  @ParameterizedTest
  void unpackShortTest(short value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packShort, PacketUnpacker::unpackShort, value);
  }

  @ValueSource(floats = {1.3F, 2.3F, 5.0F})
  @ParameterizedTest
  void unpackFloatTest(float value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packFloat, PacketUnpacker::unpackFloat, value);
  }

  @ValueSource(doubles = {100.50, 30000.3131, 50000.00})
  @ParameterizedTest
  void unpackDoubleTest(double value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packDouble, PacketUnpacker::unpackDouble, value);
  }

  @ValueSource(ints = {1, 2, 3})
  @ParameterizedTest
  void unpackMapHeaderTest(int value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packInt, PacketUnpacker::unpackInt, value);
  }

  @Test
  void unpackMapTest() throws IOException {
    final Map<String, String> value =
        Map.of(
            "test_key_1",
            "test_value_1",
            "test_key_2",
            "test_value_2",
            "test_key_3",
            "test_value_3");
    unpackValueAndAssertThatEqualTo(PacketPacker::packMap, PacketUnpacker::unpackMap, value);
  }

  @MethodSource("getInstantSubjects")
  @ParameterizedTest
  void unpackInstantTest(Instant value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packInstant, PacketUnpacker::unpackInstant, value);
  }

  @Test
  void unpackInstantWithNullValueTest() throws IOException {
    try (PacketPacker packer = getMessagePackPacketSerdesContext().newPacketPacker()) {
      packer.packDuration(null);
      try (PacketUnpacker unpacker =
          getMessagePackPacketSerdesContext().newPacketUnpacker(packer.toBinaryArray())) {
        assertThat(unpacker.unpackInstant()).isNull();
      }
    }
  }

  @MethodSource("getDurationSubjects")
  @ParameterizedTest
  void unpackDurationTest(Duration value) throws IOException {
    unpackValueAndAssertThatEqualTo(
        PacketPacker::packDuration, PacketUnpacker::unpackDuration, value);
  }

  @Test
  void unpackDurationWithNullValueTest() throws IOException {
    try (PacketPacker packer = getMessagePackPacketSerdesContext().newPacketPacker()) {
      packer.packDuration(null);
      try (PacketUnpacker unpacker =
          getMessagePackPacketSerdesContext().newPacketUnpacker(packer.toBinaryArray())) {
        assertThat(unpacker.unpackDuration()).isNull();
      }
    }
  }

  @MethodSource("getEnumSubjects")
  @ParameterizedTest
  void unpackEnumTest(GameState value) throws IOException {
    unpackValueAndAssertThatEqualTo(PacketPacker::packEnum, PacketUnpacker::unpackEnum, value);
  }

  @Test
  void unpackEnumWithNullValueTest() throws IOException {
    try (PacketPacker packer = getMessagePackPacketSerdesContext().newPacketPacker()) {
      packer.packEnum(null);
      try (PacketUnpacker unpacker =
          getMessagePackPacketSerdesContext().newPacketUnpacker(packer.toBinaryArray())) {
        assertThat((GameState) unpacker.unpackEnum()).isNull();
      }
    }
  }

  @Test
  void hasNextOnEmptyUnpackerTest() throws IOException {
    try (PacketUnpacker unpacker =
        getMessagePackPacketSerdesContext().newPacketUnpacker(new byte[0])) {
      assertThat(unpacker.hasNext()).isFalse();
    }
  }

  @Test
  void hasNextOnExhaustedUnpackerTest() throws IOException {
    try (PacketUnpacker unpacker =
        getMessagePackPacketSerdesContext().newPacketUnpacker(
            getBinaryArrayOf(PacketPacker::packInt, 1))) {
      assertThat(unpacker.hasNext()).isTrue();
    }
  }

  @Test
  void hasNextNilValueOnNilElement() throws IOException {
    try (PacketPacker packer = getMessagePackPacketSerdesContext().newPacketPacker()) {
      packer.packNil();
      try (PacketUnpacker unpacker =
          getMessagePackPacketSerdesContext().newPacketUnpacker(packer.toBinaryArray())) {
        assertThat(unpacker.hasNextNilValue()).isTrue();
      }
    }
  }

  @Test
  void hasNextNilValueOnAnyElement() throws IOException {
    try (PacketPacker packer = getMessagePackPacketSerdesContext().newPacketPacker()) {
      packer.packDuration(ZERO);
      try (PacketUnpacker unpacker =
          getMessagePackPacketSerdesContext().newPacketUnpacker(packer.toBinaryArray())) {
        assertThat(unpacker.hasNextNilValue()).isFalse();
      }
    }
  }

  @Test
  void hasNextNilValueOnExhaustedUnpackerTest() throws IOException {
    PacketUnpacker unpackerMock = mock(MessagePackPacketUnpacker.class);
    when(unpackerMock.hasNext()).thenReturn(false);
    when(unpackerMock.hasNextNilValue()).thenCallRealMethod();
    assertThat(unpackerMock.hasNextNilValue()).isFalse();
  }
}
