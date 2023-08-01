package moe.rafal.cory.packet;

import static moe.rafal.cory.packet.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;

import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketPackerFactory;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

public final class MessagePackAssertions {

  private MessagePackAssertions() {

  }

  public static <T> void assertThatPackerContains(PacketPacker packer,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T expectedValue)
      throws IOException {
    try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
      assertThatUnpackerContains(unpacker, valueResolver, expectedValue);
    }
  }

  public static <T> void assertThatUnpackerContains(PacketUnpacker unpacker,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T expectedValue)
      throws IOException {
    assertThat(valueResolver.apply(unpacker))
        .isEqualTo(expectedValue);
  }

  public static <T> void packValueAndAssertThatContains(
      PacketPacker packer,
      ThrowingBiConsumer<PacketPacker, T, IOException> packFunction,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T value) throws IOException {
    packFunction.accept(packer, value);
    assertThatPackerContains(packer, valueResolver, value);
  }

  public static <T> void unpackValueAndAssertThatEqualTo(
      ThrowingBiConsumer<PacketPacker, T, IOException> packerInitializer,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T expectedValue) throws IOException {
    try (PacketUnpacker unpacker = producePacketUnpacker(
        getBinaryArrayOf(packerInitializer, expectedValue))) {
      assertThat(valueResolver.apply(unpacker))
          .isEqualTo(expectedValue);
    }
  }

  public static <T> byte[] getBinaryArrayOf(
      ThrowingBiConsumer<PacketPacker, T, IOException> packetInitializer,
      T expectedValue)
      throws IOException {
    PacketPacker packer = PacketPackerFactory.producePacketPacker();
    packetInitializer.accept(packer, expectedValue);
    return packer.toBinaryArray();
  }
}
