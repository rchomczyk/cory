/*
 *    Copyright 2023-2024 cory
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

import static moe.rafal.cory.MessagePackAssertions.assertThatUnpackerContains;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.PacketTestsUtils.INITIAL_PASSWORD;
import static moe.rafal.cory.PacketTestsUtils.INITIAL_USERNAME;
import static moe.rafal.cory.PacketTestsUtils.MAXIMUM_RESPONSE_PERIOD;
import static moe.rafal.cory.integration.nats.EmbeddedNatsServerExtension.getNatsConnectionUri;
import static moe.rafal.cory.logger.LoggerFacade.getNoopLogger;
import static moe.rafal.cory.message.NatsMessageBrokerFactory.produceNatsMessageBroker;
import static moe.rafal.cory.message.packet.PacketPublisher.getPacketPublisher;
import static moe.rafal.cory.serdes.MessagePackPacketSerdesContext.getMessagePackPacketSerdesContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import io.nats.client.Options;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.PacketTestsUtils;
import moe.rafal.cory.integration.nats.EmbeddedNatsServerExtension;
import moe.rafal.cory.integration.nats.InjectNatsServer;
import moe.rafal.cory.logger.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketSerdesContext;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.subject.LoginPacket;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedNatsServerExtension.class)
class PackerPublisherImplTests {

  private final LoggerFacade loggerFacade = getNoopLogger();
  @InjectNatsServer private EmbeddedNatsServer natsServer;
  private MessageBroker messageBroker;
  private PacketSerdesContext serdesContext;
  private PacketPublisher packetPublisher;

  @BeforeEach
  void createMessageBrokerAndPacketPublisher() {
    messageBroker =
        produceNatsMessageBroker(
            Options.builder().server(getNatsConnectionUri(natsServer)).build());
    packetPublisher =
        getPacketPublisher(
            loggerFacade,
            messageBroker,
            PacketGateway.INSTANCE,
            getMessagePackPacketSerdesContext());
  }

  @Test
  void publishAndObserveTest() {
    LoginPacket packet = PacketTestsUtils.getLoginPacket();
    AtomicReference<byte[]> receivedPayload = new AtomicReference<>();
    messageBroker.observe(
        BROADCAST_CHANNEL_NAME,
        (channelName, replyChannelName, payload) -> receivedPayload.set(payload));
    packetPublisher.publish(BROADCAST_CHANNEL_NAME, packet);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(
            () -> {
              assertThat(receivedPayload).isNotNull();
              try (PacketUnpacker unpacker =
                  getMessagePackPacketSerdesContext().newPacketUnpacker(receivedPayload.get())) {
                assertThatUnpackerContains(
                    unpacker, PacketUnpacker::unpackString, packet.getClass().getName());
                assertThatUnpackerContains(
                    unpacker, PacketUnpacker::unpackUUID, packet.getUniqueId());
                assertThatUnpackerContains(
                    unpacker, PacketUnpacker::unpackString, INITIAL_USERNAME);
                assertThatUnpackerContains(
                    unpacker, PacketUnpacker::unpackString, INITIAL_PASSWORD);
              }
            });
  }

  @Test
  void publishShouldThrowWhenWriteFails() throws IOException {
    Packet packetMock = mock(Packet.class);
    doThrow(new IOException()).when(packetMock).write(any());
    assertThatCode(() -> packetPublisher.publish(BROADCAST_CHANNEL_NAME, packetMock))
        .isInstanceOf(PacketPublicationException.class)
        .hasMessage(
            "Could not publish packet over the message broker, because of unexpected exception.");
  }
}
