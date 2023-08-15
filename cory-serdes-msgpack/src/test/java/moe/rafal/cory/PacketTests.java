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

import static moe.rafal.cory.MessagePackAssertions.assertThatUnpackerContains;
import static moe.rafal.cory.PacketTestsUtils.DEFAULT_VALUE;
import static moe.rafal.cory.PacketTestsUtils.INCOMING_PASSWORD;
import static moe.rafal.cory.PacketTestsUtils.INCOMING_USERNAME;
import static moe.rafal.cory.PacketTestsUtils.INITIAL_PASSWORD;
import static moe.rafal.cory.PacketTestsUtils.INITIAL_USERNAME;
import static moe.rafal.cory.PacketTestsUtils.NIL_UNIQUE_ID;
import static moe.rafal.cory.PacketTestsUtils.getEmptyLoginPacket;
import static moe.rafal.cory.PacketTestsUtils.getLoginPacket;
import static moe.rafal.cory.PacketTestsUtils.getMalformedPacket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.util.UUID;
import moe.rafal.cory.serdes.MessagePackPacketPackerFactory;
import moe.rafal.cory.serdes.MessagePackPacketUnpackerFactory;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.subject.LoginPacket;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PacketTests {

  @Test
  void createPacketTest() {
    try (MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
      uuidMock.when(UUID::randomUUID).thenReturn(NIL_UNIQUE_ID);

      Packet subject = new Packet() {
        @Override
        public void write(PacketPacker packer) {
        }

        @Override
        public void read(PacketUnpacker unpacker) {
        }
      };

      assertThat(subject.getUniqueId())
          .isEqualTo(NIL_UNIQUE_ID);
    }
  }

  @Test
  void createPacketTestWithSpecifiedUniqueId() {
    Packet subject = new Packet(NIL_UNIQUE_ID) {
      @Override
      public void write(PacketPacker packer) {
      }

      @Override
      public void read(PacketUnpacker unpacker) {
      }
    };

    assertThat(subject.getUniqueId())
        .isEqualTo(NIL_UNIQUE_ID);
  }

  @Test
  void writeTest() throws IOException {
    LoginPacket packet = getLoginPacket();
    try (PacketPacker packer = MessagePackPacketPackerFactory.INSTANCE.producePacketPacker()) {
      packet.write(packer);
      try (PacketUnpacker unpacker = MessagePackPacketUnpackerFactory.INSTANCE.producePacketUnpacker(
          packer.toBinaryArray())) {
        assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString,
            INITIAL_USERNAME);
        assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString,
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
  void getUniqueIdTest() {
    LoginPacket packet = getLoginPacket();
    assertThat(packet.getUniqueId())
        .isNotNull();
  }

  @Test
  void setUniqueIdTest() {
    LoginPacket loginPacket = getEmptyLoginPacket();
    assertThat(loginPacket)
        .isNotNull();
    loginPacket.setUniqueId(NIL_UNIQUE_ID);
    assertThat(loginPacket.getUniqueId())
        .isEqualTo(NIL_UNIQUE_ID);
  }

  @Test
  void verifyEqualityWithSameReferenceTest() {
    LoginPacket packet = getLoginPacket();
    assertThat(packet.equals(packet))
        .isTrue();
  }

  @Test
  void verifyEqualityWithNullReferenceTest() {
    LoginPacket packet = getLoginPacket();
    assertThat(packet.equals(null))
        .isFalse();
  }

  @Test
  void verifyEqualityWithDifferentUniqueIdsTest() {
    Packet packet1 = getEmptyLoginPacket();
    Packet packet2 = getEmptyLoginPacket();
    assertThat(packet1.equals(packet2))
        .isFalse();
    assertThat(packet2.equals(packet1))
        .isFalse();
  }

  @Test
  void verifyEqualityWithSameUniqueIdsButDifferentPacketTypeTest() {
    Packet packet1 = getEmptyLoginPacket();
    Packet packet2 = getMalformedPacket();
    assertThat(packet1.equals(packet2))
        .isFalse();
    assertThat(packet2.equals(packet1))
        .isFalse();
  }

  @Test
  void verifyEqualityWithSameUniqueIdsButDifferentReferencesTest() {
    Packet packet1 = getEmptyLoginPacket();
    Packet packet2 = getEmptyLoginPacket();
    packet1.setUniqueId(NIL_UNIQUE_ID);
    packet2.setUniqueId(NIL_UNIQUE_ID);
    assertThat(packet1.equals(packet2))
        .isTrue();
    assertThat(packet2.equals(packet1))
        .isTrue();
  }

  @Test
  void verifyHashCodeEqualityWithSameUniqueIdsButDifferentReferences() {
    Packet packet1 = getEmptyLoginPacket();
    Packet packet2 = getEmptyLoginPacket();
    packet1.setUniqueId(NIL_UNIQUE_ID);
    packet2.setUniqueId(NIL_UNIQUE_ID);
    assertThat(packet1.hashCode())
        .isEqualTo(packet2.hashCode());
  }
}
