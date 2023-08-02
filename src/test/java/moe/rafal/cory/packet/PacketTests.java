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

package moe.rafal.cory.packet;

import static moe.rafal.cory.packet.MessagePackAssertions.assertThatUnpackerContains;
import static moe.rafal.cory.packet.MessagePackAssertions.getBinaryArrayOf;
import static moe.rafal.cory.packet.serdes.PacketPackerFactory.producePacketPacker;
import static moe.rafal.cory.packet.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.util.UUID;
import moe.rafal.cory.packet.subject.LoginPacket;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;
import moe.rafal.cory.packet.subject.MalformedPacket;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PacketTests {

  private static final UUID NIL_UNIQUE_ID = new UUID(0, 0);
  private static final String INITIAL_USERNAME = "jdoe";
  private static final String INITIAL_PASSWORD = "jdoe123";
  private static final String NEW_USERNAME = "jsmith";
  private static final String NEW_PASSWORD = "jsmith123";
  private static final String DEFAULT_VALUE = "";
  private final LoginPacket packet = new LoginPacket(
      INITIAL_USERNAME,
      INITIAL_PASSWORD);

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
    try (PacketPacker packer = producePacketPacker()) {
      packet.write(packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, INITIAL_USERNAME);
        assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, INITIAL_PASSWORD);
      }
    }
  }

  @Test
  void readTest() throws IOException {
    byte[] content = getBinaryArrayOf((packer, expectedValue) -> {
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
  void getUniqueIdTest() {
    assertThat(packet.getUniqueId())
        .isNotNull();
  }

  @Test
  void setUniqueIdTest() {
    LoginPacket loginPacket = new LoginPacket();
    assertThat(loginPacket)
        .isNotNull();
    loginPacket.setUniqueId(NIL_UNIQUE_ID);
    assertThat(loginPacket.getUniqueId())
        .isEqualTo(NIL_UNIQUE_ID);
  }

  @Test
  void verifyEqualityWithSameReferenceTest() {
    assertThat(packet.equals(packet))
        .isTrue();
  }

  @Test
  void verifyEqualityWithNullReferenceTest() {
    assertThat(packet.equals(null))
        .isFalse();
  }

  @Test
  void verifyEqualityWithDifferentUniqueIdsTest() {
    Packet packet1 = new LoginPacket();
    Packet packet2 = new LoginPacket();
    assertThat(packet1.equals(packet2))
        .isFalse();
    assertThat(packet2.equals(packet1))
        .isFalse();
  }

  @Test
  void verifyEqualityWithSameUniqueIdsButDifferentPacketTypeTest() {
    Packet packet1 = new LoginPacket();
    Packet packet2 = new MalformedPacket(INITIAL_USERNAME);
    assertThat(packet1.equals(packet2))
        .isFalse();
    assertThat(packet2.equals(packet1))
        .isFalse();
  }

  @Test
  void verifyEqualityWithSameUniqueIdsButDifferentReferencesTest() {
    Packet packet1 = new LoginPacket();
    Packet packet2 = new LoginPacket();
    packet1.setUniqueId(NIL_UNIQUE_ID);
    packet2.setUniqueId(NIL_UNIQUE_ID);
    assertThat(packet1.equals(packet2))
        .isTrue();
    assertThat(packet2.equals(packet1))
        .isTrue();
  }

  @Test
  void verifyHashCodeEqualityWithSameUniqueIdsButDifferentReferences() {
    Packet packet1 = new LoginPacket();
    Packet packet2 = new LoginPacket();
    packet1.setUniqueId(NIL_UNIQUE_ID);
    packet2.setUniqueId(NIL_UNIQUE_ID);
    assertThat(packet1.hashCode())
        .isEqualTo(packet2.hashCode());
  }
}
