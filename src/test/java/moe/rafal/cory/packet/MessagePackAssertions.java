package moe.rafal.cory.packet;

import static moe.rafal.cory.packet.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;

import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;

final class MessagePackAssertions {

  private MessagePackAssertions() {

  }

  static <T> void assertThatPackerContains(PacketPacker packer,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T expectedValue)
      throws IOException {
    try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
      assertThatUnpackerContains(unpacker, valueResolver, expectedValue);
    }
  }

  static <T> void assertThatUnpackerContains(PacketUnpacker unpacker,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T expectedValue)
      throws IOException {
    assertThat(valueResolver.apply(unpacker))
        .isEqualTo(expectedValue);
  }

  static <T> void packValueAndAssertThatContains(
      PacketPacker packer,
      ThrowingBiConsumer<PacketPacker, T, IOException> packFunction,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T value) throws IOException {
    packFunction.accept(packer, value);
    assertThatPackerContains(packer, valueResolver, value);
  }

  static <T> void unpackValueAndAssertThatEqualTo(
      ThrowingBiConsumer<PacketPacker, T, IOException> packerInitializer,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T expectedValue) throws IOException {
    try (PacketUnpacker unpacker = producePacketUnpacker(
        getBinaryArrayOf(packerInitializer, expectedValue))) {
      assertThat(valueResolver.apply(unpacker))
          .isEqualTo(expectedValue);
    }
  }

  static <T> byte[] getBinaryArrayOf(
      ThrowingBiConsumer<PacketPacker, T, IOException> packetInitializer,
      T expectedValue)
      throws IOException {
    PacketPacker packer = PacketPackerFactory.producePacketPacker();
    packetInitializer.accept(packer, expectedValue);
    return packer.toBinaryArray();
  }
}
