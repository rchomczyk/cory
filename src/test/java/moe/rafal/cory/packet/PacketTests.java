package moe.rafal.cory.packet;

import static moe.rafal.cory.packet.MessagePackAssertions.assertThatUnpackerContains;
import static moe.rafal.cory.packet.MessagePackAssertions.getBinaryArrayOf;
import static moe.rafal.cory.packet.PacketPackerFactory.producePacketPacker;
import static moe.rafal.cory.packet.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.util.UUID;
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
}
