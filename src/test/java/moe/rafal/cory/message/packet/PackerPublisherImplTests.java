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

package moe.rafal.cory.message.packet;

import static moe.rafal.cory.serdes.PacketUnpackerFactory.producePacketUnpacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.MessagePackAssertions;
import moe.rafal.cory.Packet;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.MessageBrokerFactory;
import moe.rafal.cory.message.MessageBrokerSpecification;
import moe.rafal.cory.subject.LoginPacket;
import moe.rafal.cory.serdes.PacketUnpacker;
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
    MESSAGE_BROKER = MessageBrokerFactory.produceMessageBroker(new MessageBrokerSpecification(
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
            MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString,
                packet.getClass().getName());
            MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackUUID,
                packet.getUniqueId());
            MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, EXPECTED_USERNAME);
            MessagePackAssertions.assertThatUnpackerContains(unpacker, PacketUnpacker::unpackString, EXPECTED_PASSWORD);
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
