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

import static moe.rafal.cory.PacketTestsUtils.DEFAULT_VALUE;
import static moe.rafal.cory.PacketTestsUtils.INCOMING_PASSWORD;
import static moe.rafal.cory.PacketTestsUtils.INCOMING_USERNAME;
import static moe.rafal.cory.PacketTestsUtils.INITIAL_PASSWORD;
import static moe.rafal.cory.PacketTestsUtils.INITIAL_USERNAME;
import static moe.rafal.cory.PacketTestsUtils.getLoginPacket;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import moe.rafal.cory.serdes.MessagePackPacketPackerFactory;
import moe.rafal.cory.serdes.MessagePackPacketUnpackerFactory;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.subject.LoginPacket;
import org.junit.jupiter.api.Test;

class MessagePackPacketTests {

  @Test
  void writeTest() throws IOException {
    LoginPacket packet = getLoginPacket();
    try (PacketPacker packer = MessagePackPacketPackerFactory.INSTANCE.producePacketPacker()) {
      packet.write(packer);
      try (PacketUnpacker unpacker = MessagePackPacketUnpackerFactory.INSTANCE.producePacketUnpacker(
          packer.toBinaryArray())) {
        MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString,
            INITIAL_USERNAME);
        MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString,
            INITIAL_PASSWORD);
      }
    }
  }

  @Test
  void readTest() throws IOException {
    LoginPacket packet = getLoginPacket();
    byte[] content = MessagePackAssertions.getBinaryArrayOf((packer, expectedValue) -> {
      packer.packString(INCOMING_USERNAME);
      packer.packString(INCOMING_PASSWORD);
    }, DEFAULT_VALUE);
    try (PacketUnpacker unpacker = MessagePackPacketUnpackerFactory.INSTANCE.producePacketUnpacker(
        content)) {
      packet.read(unpacker);
      assertThat(packet.getUsername())
          .isEqualTo(INCOMING_USERNAME);
      assertThat(packet.getPassword())
          .isEqualTo(INCOMING_PASSWORD);
    }
  }

  @Test
  void writeAndReadTest() throws IOException {
    LoginPacket packet = getLoginPacket();
    try (PacketPacker packer = MessagePackPacketPackerFactory.INSTANCE.producePacketPacker()) {
      packet.write(packer);
      try (PacketUnpacker unpacker = MessagePackPacketUnpackerFactory.INSTANCE.producePacketUnpacker(
          packer.toBinaryArray())) {
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
    LoginPacket packet = getLoginPacket();
    assertThat(packet.getUniqueId())
        .isNotNull();
  }
}
