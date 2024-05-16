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

import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.PacketTestsUtils.MAXIMUM_RESPONSE_PERIOD;
import static moe.rafal.cory.PacketTestsUtils.getLoginPacket;
import static moe.rafal.cory.PacketTestsUtils.getLogoutPacket;
import static moe.rafal.cory.integration.nats.EmbeddedNatsServerExtension.getNatsConnectionUri;
import static moe.rafal.cory.logger.LoggerFacade.getNoopLogger;
import static moe.rafal.cory.message.NatsMessageBrokerFactory.produceNatsMessageBroker;
import static moe.rafal.cory.message.packet.PacketListenerObserver.getPacketListenerObserver;
import static moe.rafal.cory.message.packet.PacketPublisher.getPacketPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

import io.nats.client.Options;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.integration.nats.EmbeddedNatsServerExtension;
import moe.rafal.cory.integration.nats.InjectNatsServer;
import moe.rafal.cory.logger.LoggerFacade;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.MessagePackPacketPackerFactory;
import moe.rafal.cory.serdes.MessagePackPacketUnpackerFactory;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.subject.LoginPacket;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedNatsServerExtension.class)
class PacketListenerObserverImplTests {

  private final LoggerFacade loggerFacade = getNoopLogger();
  @InjectNatsServer private EmbeddedNatsServer natsServer;
  private PacketGateway packetGateway;
  private PacketPublisher packetPublisher;
  private PacketListenerObserver packetListenerObserver;

  @BeforeEach
  void createMessageBrokerAndPacketPublisherWithPacketListenerObserver() {
    packetGateway = PacketGateway.INSTANCE;
    MessageBroker messageBroker =
        produceNatsMessageBroker(
            Options.builder().server(getNatsConnectionUri(natsServer)).build());
    packetPublisher =
        getPacketPublisher(
            loggerFacade, messageBroker, packetGateway, MessagePackPacketPackerFactory.INSTANCE);
    packetListenerObserver =
        getPacketListenerObserver(
            loggerFacade,
            messageBroker,
            packetGateway,
            packetPublisher,
            MessagePackPacketUnpackerFactory.INSTANCE);
  }

  @Test
  void observeAndPublishTest() {
    Packet packet = getLoginPacket();
    AtomicReference<LoginPacket> receivedPacket = new AtomicReference<>();
    packetListenerObserver.observe(
        BROADCAST_CHANNEL_NAME,
        new PacketListenerDelegate<>(LoginPacket.class) {
          @Override
          public void receive(String channelName, String replyChannelName, LoginPacket packet) {
            receivedPacket.set(packet);
          }
        });
    packetPublisher.publish(BROADCAST_CHANNEL_NAME, packet);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(() -> assertThat(receivedPacket.get()).isEqualTo(packet));
  }

  @Test
  void observeAndPublishShouldIgnoreNotSupportedPacketTest() {
    Packet loginPacket = getLoginPacket();
    Packet logoutPacket = getLogoutPacket();
    AtomicReference<Packet> receivedPacket = new AtomicReference<>();
    packetListenerObserver.observe(
        BROADCAST_CHANNEL_NAME,
        new PacketListenerDelegate<>(LoginPacket.class) {
          @Override
          public void receive(String channelName, String replyChannelName, LoginPacket packet) {
            receivedPacket.set(packet);
          }
        });
    packetPublisher.publish(BROADCAST_CHANNEL_NAME, logoutPacket);
    packetPublisher.publish(BROADCAST_CHANNEL_NAME, loginPacket);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(
            () -> {
              assertThat(receivedPacket.get()).isNotEqualTo(logoutPacket);
              assertThat(receivedPacket.get()).isEqualTo(loginPacket);
            });
  }

  @Test
  void processIncomingPacketTest() throws IOException {
    try (PacketPacker packer = MessagePackPacketPackerFactory.INSTANCE.getPacketPacker()) {
      LoginPacket packet = getLoginPacket();
      packetGateway.writePacket(packet, packer);
      LoginPacket processedPacket =
          packetListenerObserver.processIncomingPacket(packer.toBinaryArray());
      assertThat(processedPacket).isEqualTo(packet);
    }
  }

  @Test
  void processIncomingPacketShouldThrowWhenMalformedTest() throws IOException {
    try (PacketPacker packer = MessagePackPacketPackerFactory.INSTANCE.getPacketPacker()) {
      packer.packString("Hello");
      packer.packString("World");
      byte[] content = packer.toBinaryArray();
      assertThatCode(() -> packetListenerObserver.processIncomingPacket(content))
          .isInstanceOf(PacketProcessingException.class)
          .hasMessage("Could not process incoming packet, because of unexpected exception.");
    }
  }
}
