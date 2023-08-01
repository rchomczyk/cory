package moe.rafal.cory.packet.serdes;

import static moe.rafal.cory.packet.MessagePackAssertions.getBinaryArrayOf;
import static moe.rafal.cory.packet.MessagePackAssertions.unpackValueAndAssertThatEqualTo;
import static moe.rafal.cory.packet.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
}
