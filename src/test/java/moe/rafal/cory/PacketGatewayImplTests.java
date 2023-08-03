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

import static moe.rafal.cory.serdes.PacketPackerFactory.producePacketPacker;
import static moe.rafal.cory.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import moe.rafal.cory.subject.LoginPacket;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.subject.MalformedPacket;
import org.junit.jupiter.api.Test;

class PacketGatewayImplTests {

  private final PacketGateway packetGateway = PacketGateway.INSTANCE;

  @Test
  void writeAndReadPacketTest() throws IOException {
    LoginPacket packet = PacketTestsUtils.getLoginPacket();
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writePacket(packet, packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThat((LoginPacket) packetGateway.readPacket(unpacker))
            .isEqualTo(packet);
      }
    }
  }

  @Test
  void writeAndReadPacketShouldThrowWithMissingConstructorTest() throws IOException {
    MalformedPacket packet = PacketTestsUtils.getMalformedPacket();
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writePacket(packet, packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThatCode(() -> packetGateway.readPacket(unpacker))
            .isInstanceOf(MalformedPacketException.class)
            .hasMessage(
                "Packet could not be produced, because of missing public constructor without any parameters.");
      }
    }
  }

  @Test
  void writeAndReadPacketTypeTest() throws IOException {
    LoginPacket packet = PacketTestsUtils.getLoginPacket();
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writePacketType(packet, packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThat(packetGateway.readPacketType(unpacker))
            .isEqualTo(packet.getClass());
      }
    }
  }

  @Test
  void writeAndReadPacketTypeShouldThrowWithMissingTypeTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packer.packString("moe.rafal.cory.packet.subject.MissingPacket");
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThatCode(() -> packetGateway.readPacketType(unpacker))
            .isInstanceOf(MalformedPacketException.class)
            .hasMessage(
                "Packet definition seems to be malformed, as packet type could not be found in classpath.");
      }
    }
  }

  @Test
  void writeAndReadPacketUniqueIdTest() throws IOException {
    LoginPacket packet = PacketTestsUtils.getLoginPacket();
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writePacketUniqueId(packet, packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThat(packetGateway.readPacketUniqueId(unpacker))
            .isEqualTo(packet.getUniqueId());
      }
    }
  }
}
