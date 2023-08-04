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

import static moe.rafal.cory.MessagePackAssertions.packValueAndAssertThatContains;
import static moe.rafal.cory.serdes.PacketPackerFactory.producePacketPacker;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MessagePackPacketPackerTests {

  private static final UUID NIL_UNIQUE_ID = new UUID(0, 0);
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

  @Test
  void packUUIDTest() throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packUUID,
        PacketUnpacker::unpackUUID, NIL_UNIQUE_ID);
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

  @Test
  void packInstantTest() throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packInstant,
        PacketUnpacker::unpackInstant, Instant.now());
  }

  @Test
  void packDurationTest() throws IOException {
    packValueAndAssertThatContains(packetPacker,
        PacketPacker::packDuration,
        PacketUnpacker::unpackDuration, Duration.ofSeconds(30));
  }
}
