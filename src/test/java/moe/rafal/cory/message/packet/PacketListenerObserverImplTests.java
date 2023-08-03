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

import static moe.rafal.cory.PacketTestsUtils.getLoginPacket;
import static moe.rafal.cory.PacketTestsUtils.getLogoutPacket;
import static moe.rafal.cory.integration.EmbeddedNatsServerExtension.getNatsConnectionUri;
import static moe.rafal.cory.message.MessageBrokerFactory.produceMessageBroker;
import static moe.rafal.cory.serdes.PacketPackerFactory.producePacketPacker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.integration.EmbeddedNatsServerExtension;
import moe.rafal.cory.integration.InjectNatsServer;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.MessageBrokerSpecification;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.subject.LoginPacket;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedNatsServerExtension.class)
class PacketListenerObserverImplTests {

  private static final Duration MAXIMUM_RESPONSE_PERIOD = Duration.ofSeconds(2);
  private static final String BROADCAST_CHANNEL_NAME = "test-channel";
  @InjectNatsServer
  private EmbeddedNatsServer natsServer;
  private PacketGateway packetGateway;
  private MessageBroker messageBroker;
  private PacketPublisher packetPublisher;
  private PacketListenerObserverImpl packetListenerObserver;

  @BeforeEach
  void createMessageBrokerAndPacketPublisherWithPacketListenerObserver() {
    packetGateway = PacketGateway.INSTANCE;
    messageBroker = produceMessageBroker(new MessageBrokerSpecification(
        getNatsConnectionUri(natsServer), "", ""));
    packetPublisher = new PacketPublisherImpl(messageBroker);
    packetListenerObserver = new PacketListenerObserverImpl(messageBroker, packetGateway);
  }

  @Test
  void observeAndPublishTest() {
    Packet packet = getLoginPacket();
    AtomicReference<LoginPacket> receivedPacket = new AtomicReference<>();
    packetListenerObserver.observe(BROADCAST_CHANNEL_NAME,
        new PacketListenerDelegate<>(LoginPacket.class) {
          @Override
          public void receive(String channelName, LoginPacket packet) {
            receivedPacket.set(packet);
          }
        });
    packetPublisher.publish(BROADCAST_CHANNEL_NAME, packet);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(() -> assertThat(receivedPacket.get())
            .isEqualTo(packet));
  }

  @Test
  void observeAndPublishShouldIgnoreNotSupportedPacketTest() {
    Packet loginPacket = getLoginPacket();
    Packet logoutPacket = getLogoutPacket();
    AtomicReference<Packet> receivedPacket = new AtomicReference<>();
    packetListenerObserver.observe(BROADCAST_CHANNEL_NAME,
        new PacketListenerDelegate<>(LoginPacket.class) {
          @Override
          public void receive(String channelName, LoginPacket packet) {
            receivedPacket.set(packet);
          }
        });
    packetPublisher.publish(BROADCAST_CHANNEL_NAME, logoutPacket);
    packetPublisher.publish(BROADCAST_CHANNEL_NAME, loginPacket);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(() -> {
          assertThat(receivedPacket.get())
              .isNotEqualTo(logoutPacket);
          assertThat(receivedPacket.get())
              .isEqualTo(loginPacket);
        });
  }

  @Test
  void processIncomingPacketTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      LoginPacket packet = getLoginPacket();
      packetGateway.writePacket(packet, packer);
      LoginPacket processedPacket = packetListenerObserver.processIncomingPacket(
          packer.toBinaryArray());
      assertThat(processedPacket)
          .isEqualTo(packet);
    }
  }

  @Test
  void processIncomingPacketShouldThrowWhenMalformedTest() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packer.packString("Hello");
      packer.packString("World");
      byte[] content = packer.toBinaryArray();
      assertThatCode(() -> packetListenerObserver.processIncomingPacket(content))
          .isInstanceOf(PacketProcessingException.class)
          .hasMessage("Could not process incoming packet, because of unexpected exception.");
    }
  }
}
