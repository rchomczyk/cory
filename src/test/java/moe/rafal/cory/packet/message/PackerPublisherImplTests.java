package moe.rafal.cory.packet.message;

import static moe.rafal.cory.packet.MessagePackAssertions.assertThatUnpackerContains;
import static moe.rafal.cory.packet.message.MessageBrokerFactory.produceMessageBroker;
import static moe.rafal.cory.packet.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.packet.LoginPacket;
import moe.rafal.cory.packet.Packet;
import moe.rafal.cory.packet.serdes.PacketUnpacker;
import np.com.madanpokharel.embed.nats.EmbeddedNatsConfig;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PackerPublisherImplTests {

  private static final Duration MAXIMUM_RESPONSE_PERIOD = Duration.ofSeconds(2);
  private static final String EXPECTED_USERNAME = "shitzuu";
  private static final String EXPECTED_PASSWORD = "my-secret-password-123";
  private static final String EXPECTED_CHANNEL_NAME = "test-channel";
  private static final EmbeddedNatsServer EMBEDDED_SERVER = new EmbeddedNatsServer(
      EmbeddedNatsConfig.defaultNatsServerConfig());
  private static MessageBroker MESSAGE_BROKER;
  private static PacketPublisher PACKET_PUBLISHER;
  private final Packet packet = new LoginPacket(
      EXPECTED_USERNAME,
      EXPECTED_PASSWORD);

  @BeforeAll
  static void startEmbeddedServerAndCreateMessageBroker() throws Exception {
    EMBEDDED_SERVER.startServer();
    MESSAGE_BROKER = produceMessageBroker(new MessageBrokerSpecification(
        getEmbeddedServerConnectionUri(),
        EXPECTED_USERNAME,
        EXPECTED_PASSWORD));
    PACKET_PUBLISHER = new PacketPublisherImpl(MESSAGE_BROKER);
  }

  @AfterAll
  static void ditchEmbeddedServer() {
    EMBEDDED_SERVER.stopServer();
  }

  private static String getEmbeddedServerConnectionUri() {
    return String.format("nats://%s:%d",
        EMBEDDED_SERVER.getRunningHost(),
        EMBEDDED_SERVER.getRunningPort());
  }

  @Test
  void publishAndObserveTest() {
    AtomicReference<byte[]> receivedPayload = new AtomicReference<>();
    MESSAGE_BROKER.observe(EXPECTED_CHANNEL_NAME,
        (channelName, payload) -> receivedPayload.set(payload));
    PACKET_PUBLISHER.publish(EXPECTED_CHANNEL_NAME, packet);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(() -> {
          assertThat(receivedPayload)
              .isNotNull();
          try (PacketUnpacker unpacker = producePacketUnpacker(receivedPayload.get())) {
            assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString,
                packet.getClass().getName());
            assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, EXPECTED_USERNAME);
            assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, EXPECTED_PASSWORD);
          }
        });
  }

  @Test
  void publishShouldThrowWhenWriteFails() throws IOException {
    Packet packetMock = mock(Packet.class);
    doThrow(new IOException())
        .when(packetMock)
        .write(any());
    assertThatCode(() -> PACKET_PUBLISHER.publish(EXPECTED_CHANNEL_NAME, packetMock))
        .isInstanceOf(PacketPublicationException.class);
  }
}
