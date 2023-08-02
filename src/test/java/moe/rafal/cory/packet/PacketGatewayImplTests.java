package moe.rafal.cory.packet;

import static moe.rafal.cory.packet.MessagePackAssertions.assertThatPackerContains;
import static moe.rafal.cory.packet.serdes.PacketPackerFactory.producePacketPacker;
import static moe.rafal.cory.packet.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import moe.rafal.cory.packet.serdes.PacketPacker;
import moe.rafal.cory.packet.serdes.PacketUnpacker;
import org.junit.jupiter.api.Test;

class PacketGatewayImplTests {

  private static final String INITIAL_USERNAME = "shitzuu";
  private static final String INITIAL_PASSWORD = "my-secret-password-123";
  private final PacketGateway packetGateway = PacketGateway.INSTANCE;
  private final Packet packet = new LoginPacket(
      INITIAL_USERNAME,
      INITIAL_PASSWORD);

  @Test
  void mutateTest() {
    assertThat(packetGateway.mutate(LoginPacket.class, packet))
        .isInstanceOf(LoginPacket.class);
  }

  @Test
  void writeDefinitionTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writeDefinition(packet.getClass(), packer);
      assertThatPackerContains(packer, PacketUnpacker::unpackString, packet.getClass().getName());
    }
  }

  @Test
  void readDefinitionTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writeDefinition(packet.getClass(), packer);
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThat(packetGateway.readDefinition(unpacker))
            .isEqualTo(packet.getClass());
      }
    }
  }

  @Test
  void readDefinitionWithMalformedDefinitionTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packer.packString("moe.rafal.cory.packet.MalformedPacket");
      try (PacketUnpacker unpacker = producePacketUnpacker(packer.toBinaryArray())) {
        assertThatCode(() -> packetGateway.readDefinition(unpacker))
            .isInstanceOf(PacketMalformedDefinitionException.class);
      }
    }
  }
}
