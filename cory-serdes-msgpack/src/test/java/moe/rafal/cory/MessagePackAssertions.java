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

package moe.rafal.cory;

import static org.assertj.core.api.Assertions.assertThat;

import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingFunction;
import java.io.IOException;
import moe.rafal.cory.serdes.MessagePackPacketPackerFactory;
import moe.rafal.cory.serdes.MessagePackPacketUnpackerFactory;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;

public final class MessagePackAssertions {

  private MessagePackAssertions() {

  }

  public static <T> void assertThatPackerContains(PacketPacker packer,
      ThrowingFunction<PacketUnpacker, T, IOException> valueResolver,
      T expectedValue)
      throws IOException {
    try (PacketUnpacker unpacker = MessagePackPacketUnpackerFactory.INSTANCE.producePacketUnpacker(
        packer.toBinaryArray())) {
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
    try (PacketUnpacker unpacker = MessagePackPacketUnpackerFactory.INSTANCE.producePacketUnpacker(
        getBinaryArrayOf(packerInitializer, expectedValue))) {
      assertThat(valueResolver.apply(unpacker))
          .isEqualTo(expectedValue);
    }
  }

  public static <T> byte[] getBinaryArrayOf(
      ThrowingBiConsumer<PacketPacker, T, IOException> packetInitializer,
      T expectedValue)
      throws IOException {
    PacketPacker packer = MessagePackPacketPackerFactory.INSTANCE.producePacketPacker();
    packetInitializer.accept(packer, expectedValue);
    return packer.toBinaryArray();
  }
}
