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

package moe.rafal.cory.message;

import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.PacketTestsUtils.MAXIMUM_RESPONSE_PERIOD;
import static moe.rafal.cory.PacketTestsUtils.getLoginPacket;
import static moe.rafal.cory.PacketTestsUtils.getLoginRequestPacket;
import static moe.rafal.cory.integration.nats.EmbeddedNatsServerExtension.getNatsConnectionUri;
import static moe.rafal.cory.message.NatsMessageBrokerFactory.produceNatsMessageBroker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nats.client.Options;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.Cory;
import moe.rafal.cory.CoryBuilder;
import moe.rafal.cory.Packet;
import moe.rafal.cory.integration.nats.EmbeddedNatsServerExtension;
import moe.rafal.cory.integration.nats.InjectNatsServer;
import moe.rafal.cory.message.packet.PacketListenerDelegate;
import moe.rafal.cory.serdes.MessagePackPacketPackerFactory;
import moe.rafal.cory.serdes.MessagePackPacketUnpackerFactory;
import moe.rafal.cory.subject.LoginPacket;
import moe.rafal.cory.subject.LoginRequestPacket;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedNatsServerExtension.class)
class CoryImplTests {

  @InjectNatsServer
  private EmbeddedNatsServer natsServer;
  private Cory cory;

  @BeforeEach
  void setupCory() {
    cory = CoryBuilder.newBuilder()
        .withMessageBroker(produceNatsMessageBroker(Options.builder()
            .server(getNatsConnectionUri(natsServer))
            .build()))
        .withPacketPackerFactory(MessagePackPacketPackerFactory.INSTANCE)
        .withPacketUnpackerFactory(MessagePackPacketUnpackerFactory.INSTANCE)
        .build();
  }

  @AfterEach
  void ditchCory() throws IOException {
    cory.close();
  }

  @Test
  void publishAndObserveTest() {
    LoginPacket packet = getLoginPacket();
    AtomicReference<Packet> receivedPacket = new AtomicReference<>();
    cory.observe(BROADCAST_CHANNEL_NAME, new PacketListenerDelegate<>(LoginPacket.class) {
      @Override
      public void receive(String channelName, String replyChannelName, LoginPacket packet) {
        receivedPacket.set(packet);
      }
    });
    cory.publish(BROADCAST_CHANNEL_NAME, packet);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(() -> assertThat(receivedPacket.get())
            .isEqualTo(packet));
  }

  @Test
  void requestTest() {
    LoginRequestPacket packet = getLoginRequestPacket();
    AtomicReference<LoginRequestPacket> receivedPacket = new AtomicReference<>();
    cory.observe(BROADCAST_CHANNEL_NAME, new PacketListenerDelegate<>(LoginRequestPacket.class) {
      @Override
      public void receive(String channelName, String replyChannelName, LoginRequestPacket packet) {
        packet.setAccess(true);
        cory.publish(replyChannelName, packet);
      }
    });
    cory.request(BROADCAST_CHANNEL_NAME, packet)
        .thenAccept(response -> receivedPacket.set((LoginRequestPacket) response));
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(() -> assertTrue(receivedPacket.get().hasAccess()));
  }

  @Test
  void closeTest() {
    assertThatCode(() -> cory.close())
        .doesNotThrowAnyException();
  }
}
