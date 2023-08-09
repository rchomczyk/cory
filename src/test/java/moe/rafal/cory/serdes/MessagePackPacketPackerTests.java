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

import static java.lang.Byte.MAX_VALUE;
import static java.lang.Byte.MIN_VALUE;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofSeconds;
import static moe.rafal.cory.MessagePackAssertions.packValueAndAssertThatContains;
import static moe.rafal.cory.serdes.PacketPackerFactory.producePacketPacker;
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

class MessagePackPacketPackerTests {

  private final PacketPacker packetPacker = producePacketPacker();

  @AfterEach
  void flushAndClosePacketPacker() throws IOException {
    packetPacker.flush();
  }

  @Test
  void flushPacketUnpacker() {
    assertThatCode(packetPacker::flush)
        .doesNotThrowAnyException();
  }

  @Test
  void closePacketUnpacker() {
    assertThatCode(() -> producePacketPacker().close())
        .doesNotThrowAnyException();
  }

  @ValueSource(ints = {10, 20, 30})
  @ParameterizedTest
  void packArrayHeaderTest(int value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packArrayHeader,
        PacketUnpacker::unpackArrayHeader, value);
  }

  @ValueSource(ints = {40, 50, 60})
  @ParameterizedTest
  void packBinaryHeaderTest(int value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packBinaryHeader,
        PacketUnpacker::unpackBinaryHeader, value);
  }


  @MethodSource("getBinarySubjects")
  @ParameterizedTest
  void packBinary(byte[] value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        (packer, givenValue) -> {
          packer.packBinaryHeader(givenValue.length);
          packer.packPayload(givenValue);
        },
        PacketUnpacker::unpackPayload, value);

  }

  private static Set<byte[]> getBinarySubjects() {
    return Set.of(
        new byte[] {1, 2, 3, 4, -1},
        new byte[] {MIN_VALUE, MAX_VALUE},
        new byte[] {60, 90, 30, 110});
  }

  @ValueSource(strings = {"test_string_1", "test_string_2", "test_string_3"})
  @ParameterizedTest
  void packStringTest(String value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packString,
        PacketUnpacker::unpackString, value);
  }

  @ValueSource(booleans = {true, false})
  @ParameterizedTest
  void packBooleanTest(boolean value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packBoolean,
        PacketUnpacker::unpackBoolean, value);
  }

  @ValueSource(ints = {600, 700, 800})
  @ParameterizedTest
  void packIntTest(int value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packInt,
        PacketUnpacker::unpackInt, value);
  }

  @ValueSource(bytes = {0, 1})
  @ParameterizedTest
  void packByteTest(byte value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packByte,
        PacketUnpacker::unpackByte, value);
  }

  @ValueSource(longs = {100000L, 200000000L, 300000000000L})
  @ParameterizedTest
  void packLongTest(long value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packLong,
        PacketUnpacker::unpackLong, value);
  }

  @MethodSource("getUuidSubjects")
  @ParameterizedTest
  void packUUIDTest(UUID value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
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
  void packShortTest(short value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packShort,
        PacketUnpacker::unpackShort, value);
  }

  @ValueSource(floats = {1.3F, 2.3F, 5.0F})
  @ParameterizedTest
  void packFloatTest(float value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packFloat,
        PacketUnpacker::unpackFloat, value);
  }

  @ValueSource(doubles = {100.50, 30000.3131, 50000.00})
  @ParameterizedTest
  void packDoubleTest(double value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packDouble,
        PacketUnpacker::unpackDouble, value);
  }

  @ValueSource(ints = {1, 2, 3})
  @ParameterizedTest
  void packMapHeaderTest(int value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packMapHeader,
        PacketUnpacker::unpackMapHeader, value);
  }

  @MethodSource("getInstantSubjects")
  @ParameterizedTest
  void packInstantTest(Instant value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packInstant,
        PacketUnpacker::unpackInstant, value);
  }

  @Test
  void packInstantWithNullValueTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packer.packInstant(null);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThat(unpacker.unpackInstant())
            .isNull();
      }
    }
  }

  private static Set<Instant> getInstantSubjects() {
    return Set.of(
        Instant.parse("2023-08-01T12:00:00.00Z"),
        Instant.parse("2023-08-02T12:00:00.00Z"),
        Instant.parse("2023-08-03T12:00:00.00Z"));
  }

  @MethodSource("getDurationSubjects")
  @ParameterizedTest
  void packDurationTest(Duration value) throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packDuration,
        PacketUnpacker::unpackDuration, value);
  }

  @Test
  void packDurationWithNullValueTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packer.packDuration(null);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThat(unpacker.unpackDuration())
            .isNull();
      }
    }
  }

  private static Set<Duration> getDurationSubjects() {
    return Set.of(ofSeconds(30), ofHours(2), ofDays(1));
  }
}
