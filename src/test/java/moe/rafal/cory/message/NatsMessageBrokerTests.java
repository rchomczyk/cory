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

package moe.rafal.cory.message;

import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_REQUEST_TEST_PAYLOAD;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_TEST_PAYLOAD;
import static moe.rafal.cory.PacketTestsUtils.MAXIMUM_RESPONSE_PERIOD;
import static moe.rafal.cory.integration.EmbeddedNatsServerExtension.getNatsConnectionUri;
import static moe.rafal.cory.message.NatsMessageBrokerFactory.produceNatsMessageBroker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

import io.nats.client.Options;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.integration.EmbeddedNatsServerExtension;
import moe.rafal.cory.integration.InjectNatsServer;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedNatsServerExtension.class)
class NatsMessageBrokerTests {

  @InjectNatsServer
  private EmbeddedNatsServer natsServer;
  private MessageBroker messageBroker;

  @BeforeEach
  void createMessageBroker() {
    messageBroker = produceNatsMessageBroker(Options.builder()
        .server(getNatsConnectionUri(natsServer))
        .build());
  }

  @Test
  void publishAndObserveTest() {
    AtomicBoolean receivedPayload = new AtomicBoolean();
    messageBroker.observe(BROADCAST_CHANNEL_NAME,
        (channelName, payload, replyChannelName) -> receivedPayload.set(true));
    messageBroker.publish(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilTrue(receivedPayload);
  }

  @Test
  void requestTest() {
    AtomicReference<byte[]> receivedPayload = new AtomicReference<>();
    messageBroker.observe(BROADCAST_CHANNEL_NAME,
        (channelName, replyChannelName, payload) -> messageBroker.publish(replyChannelName,
            BROADCAST_REQUEST_TEST_PAYLOAD));
    messageBroker.request(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD).thenAccept(
        receivedPayload::set);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(() -> assertThat(receivedPayload.get())
            .isEqualTo(BROADCAST_REQUEST_TEST_PAYLOAD));
  }

  @Test
  void closeTest() throws IOException {
    messageBroker.close();
    assertThatCode(() -> messageBroker.publish(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD))
        .isInstanceOf(IllegalStateException.class);
  }
}
