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

import static java.lang.String.format;
import static java.time.Duration.ZERO;
import static java.util.UUID.randomUUID;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME_SECOND;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_REQUEST_TEST_PAYLOAD;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_TEST_PAYLOAD;
import static moe.rafal.cory.PacketTestsUtils.MAXIMUM_RESPONSE_PERIOD;
import static moe.rafal.cory.integration.redis.EmbeddedRedisServerExtension.getRedisConnectionUri;
import static moe.rafal.cory.message.RedisMessageBrokerFactory.getRedisMessageBroker;
import static moe.rafal.cory.serdes.MessagePackPacketSerdesContext.getMessagePackPacketSerdesContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.github.fppt.jedismock.RedisServer;
import io.lettuce.core.RedisURI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import moe.rafal.cory.integration.redis.EmbeddedRedisServerExtension;
import moe.rafal.cory.integration.redis.InjectRedisServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedRedisServerExtension.class)
class RedisMessageBrokerTests {

  @InjectRedisServer private RedisServer redisServer;
  private RedisMessageBroker messageBroker;
  private RedisMessageBroker messageBrokerWhichIsFailing;

  @BeforeEach
  void createMessageBroker() {
    messageBroker =
        (RedisMessageBroker)
            getRedisMessageBroker(
                getMessagePackPacketSerdesContext(),
                RedisURI.create(getRedisConnectionUri(redisServer)));
    messageBrokerWhichIsFailing =
        (RedisMessageBroker)
            RedisMessageBrokerFactory.getRedisMessageBroker(
                getMessagePackPacketSerdesContext(),
                RedisURI.create(getRedisConnectionUri(redisServer)),
                ZERO);
  }

  @Test
  void publishAndObserveTest() {
    AtomicBoolean receivedPayload = new AtomicBoolean();
    messageBroker.observe(
        BROADCAST_CHANNEL_NAME,
        (channelName, payload, replyChannelName) -> receivedPayload.set(true));
    messageBroker.publish(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD);
    await().atMost(MAXIMUM_RESPONSE_PERIOD).untilTrue(receivedPayload);
  }

  @Test
  void beginTopicObservationShouldIgnoreTest() {
    RedisMessageBroker redisMessageBrokerMock = spy(messageBroker);
    redisMessageBrokerMock.observe(
        BROADCAST_CHANNEL_NAME, ((channelName, replyChannelName, payload) -> {}));
    redisMessageBrokerMock.observe(
        BROADCAST_CHANNEL_NAME, ((channelName, replyChannelName, payload) -> {}));
    verify(redisMessageBrokerMock).beginTopicObservation(any());
  }

  @Test
  void beginTopicObservationShouldBeCalledTest() {
    RedisMessageBroker redisMessageBrokerMock = spy(messageBroker);
    redisMessageBrokerMock.observe(
        BROADCAST_CHANNEL_NAME, ((channelName, replyChannelName, payload) -> {}));
    verify(redisMessageBrokerMock).beginTopicObservation(any());
  }

  @Test
  void requestTest() {
    AtomicReference<byte[]> receivedPayload = new AtomicReference<>();
    messageBroker.observe(
        BROADCAST_CHANNEL_NAME,
        (channelName, replyChannelName, payload) ->
            messageBroker.publish(replyChannelName, BROADCAST_REQUEST_TEST_PAYLOAD));
    messageBroker
        .request(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD)
        .thenAccept(receivedPayload::set);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilAsserted(
            () -> assertThat(receivedPayload.get()).isEqualTo(BROADCAST_REQUEST_TEST_PAYLOAD));
  }

  @Test
  void requestShouldIgnoreAnotherTopicTest() {
    AtomicBoolean whetherPayloadWasReceived = new AtomicBoolean();
    messageBroker.observe(
        BROADCAST_CHANNEL_NAME,
        (channelName, replyChannelName, payload) ->
            messageBroker.publish(BROADCAST_CHANNEL_NAME_SECOND, BROADCAST_REQUEST_TEST_PAYLOAD));
    messageBroker.request(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD);
    await()
        .during(MAXIMUM_RESPONSE_PERIOD.minusSeconds(1))
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilFalse(whetherPayloadWasReceived);
  }

  @Test
  void requestShouldHandleProcessingResponseFailureTest() {
    assertThatCode(
            () ->
                messageBrokerWhichIsFailing
                    .request(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD)
                    .join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(MessageProcessingException.class)
        .hasMessage(
            format(
                "%s: %s",
                MessageProcessingException.class.getName(),
                "Could not process incoming response, because of unexpected exception."));
  }

  @Test
  void requestShouldHandleProcessingResponseFailureWhenObservingTest() {
    RedisMessageBroker redisMessageBrokerMock = spy(messageBrokerWhichIsFailing);
    doNothing().when(redisMessageBrokerMock).cancelTopicObservation(any());
    redisMessageBrokerMock.observe(
        BROADCAST_CHANNEL_NAME, ((channelName, replyChannelName, payload) -> {}));
    assertThatCode(
            () ->
                redisMessageBrokerMock
                    .request(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD)
                    .join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(MessageProcessingException.class)
        .hasMessage(
            format(
                "%s: %s",
                MessageProcessingException.class.getName(),
                "Could not process incoming response, because of unexpected exception."));
    verify(redisMessageBrokerMock).cancelTopicObservation(any());
  }

  @Test
  void responseShouldNotArriveIfAnotherTopicTest() {
    String generatedChannelName = randomUUID().toString();
    CompletableFuture<byte[]> responseFuture = new CompletableFuture<>();
    messageBroker.observe(
        BROADCAST_CHANNEL_NAME,
        new RedisRequestMessageListener(generatedChannelName, responseFuture));
    messageBroker.publish(BROADCAST_CHANNEL_NAME_SECOND, BROADCAST_TEST_PAYLOAD);
    await()
        .during(MAXIMUM_RESPONSE_PERIOD.minusSeconds(1))
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .until(() -> !responseFuture.isDone());
  }

  @Test
  void closeTest() {
    messageBroker.close();
    assertThatCode(() -> messageBroker.publish(BROADCAST_CHANNEL_NAME, BROADCAST_TEST_PAYLOAD))
        .isInstanceOf(RuntimeException.class);
  }
}
