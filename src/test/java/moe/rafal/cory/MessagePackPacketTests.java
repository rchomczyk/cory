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

import static moe.rafal.cory.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import moe.rafal.cory.subject.LoginPacket;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.serdes.PacketPackerFactory;
import org.junit.jupiter.api.Test;

class MessagePackPacketTests {

  private static final String INITIAL_USERNAME = "jdoe";
  private static final String INITIAL_PASSWORD = "jdoe123";
  private static final String NEW_USERNAME = "jsmith";
  private static final String NEW_PASSWORD = "jsmith123";
  private static final String DEFAULT_VALUE = "";
  private final MessagePackPacket packet = new MessagePackPacket(
      INITIAL_USERNAME,
      INITIAL_PASSWORD);

  @Test
  void writeTest() throws IOException {
    try (PacketPacker packer = PacketPackerFactory.producePacketPacker()) {
      packet.write(packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, INITIAL_USERNAME);
        MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, INITIAL_PASSWORD);
      }
    }
  }

  @Test
  void readTest() throws IOException {
    byte[] content = MessagePackAssertions.getBinaryArrayOf((packer, expectedValue) -> {
      packer.packString(NEW_USERNAME);
      packer.packString(NEW_PASSWORD);
    }, DEFAULT_VALUE);
    try (PacketUnpacker unpacker = producePacketUnpacker(content)) {
      packet.read(unpacker);
      assertThat(packet.getUsername())
          .isEqualTo(NEW_USERNAME);
      assertThat(packet.getPassword())
          .isEqualTo(NEW_PASSWORD);
    }
  }

  @Test
  void writeAndReadTest() throws IOException {
    try (PacketPacker packer = PacketPackerFactory.producePacketPacker()) {
      packet.write(packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        LoginPacket clonePacket = new LoginPacket();
        clonePacket.read(unpacker);
        assertThat(clonePacket.getUsername())
            .isEqualTo(INITIAL_USERNAME);
        assertThat(clonePacket.getPassword())
            .isEqualTo(INITIAL_PASSWORD);
      }
    }
  }

  @Test
  void getUniqueIdTest() {
    assertThat(packet.getUniqueId())
        .isNotNull();
  }
}
