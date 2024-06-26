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

import static io.lettuce.core.support.ConnectionPoolSupport.createGenericObjectPool;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.concurrent.CompletableFutureUtils;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketSerdesContext;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jetbrains.annotations.VisibleForTesting;

class RedisMessageBroker implements MessageBroker {

  private static final RedisCodec<String, byte[]> DEFAULT_CODEC = new RedisBinaryCodec();
  private final PacketSerdesContext serdesContext;
  private final Duration requestCleanupInterval;
  private final RedisClient redisClient;
  private final GenericObjectPool<StatefulRedisConnection<String, byte[]>> connectionPool;
  private final StatefulRedisPubSubConnection<String, byte[]> subscribingConnection;
  private final Set<String> subscribedTopics;

  RedisMessageBroker(
      PacketSerdesContext serdesContext, RedisURI redisUri, Duration requestCleanupInterval) {
    this.serdesContext = serdesContext;
    this.redisClient = RedisClient.create(redisUri);
    this.requestCleanupInterval = requestCleanupInterval;
    this.connectionPool = createRedisConnectionPool();
    this.subscribingConnection = redisClient.connectPubSub(DEFAULT_CODEC);
    this.subscribedTopics = new HashSet<>();
  }

  private GenericObjectPool<StatefulRedisConnection<String, byte[]>> createRedisConnectionPool() {
    return createGenericObjectPool(
        () -> redisClient.connect(DEFAULT_CODEC), new GenericObjectPoolConfig<>());
  }

  @Override
  public void publish(String channelName, byte[] payload) {
    publishWithHeader(channelName, payload, randomUUID());
  }

  @Override
  public void observe(String channelName, MessageListener listener) {
    subscribingConnection.addListener(
        new RedisMessageListener(serdesContext, channelName, listener));
    if (whetherSubscriptionExists(channelName)) {
      return;
    }
    beginTopicObservation(channelName);
  }

  @VisibleForTesting
  void beginTopicObservation(String channelName) {
    subscribedTopics.add(channelName);
    subscribingConnection.sync().subscribe(channelName);
  }

  @Override
  public CompletableFuture<byte[]> request(String channelName, byte[] payload) {
    UUID payloadUniqueId = randomUUID();

    CompletableFuture<byte[]> promisedResponse =
        new CompletableFuture<byte[]>()
            .orTimeout(requestCleanupInterval.toNanos(), NANOSECONDS)
            .exceptionally(
                exception ->
                    handleResponseProcessingFailure(exception, channelName, payloadUniqueId));

    observe(
        payloadUniqueId.toString(),
        new RedisRequestMessageListener(payloadUniqueId.toString(), promisedResponse));
    publishWithHeader(channelName, payload, payloadUniqueId);
    return promisedResponse;
  }

  @VisibleForTesting
  <T> T handleResponseProcessingFailure(
      Throwable exceptionCause, String channelName, UUID payloadUniqueId) {
    if (whetherSubscriptionExists(channelName)) {
      cancelTopicObservation(payloadUniqueId.toString());
    }
    throw new MessageProcessingException(
        "Could not process incoming response, because of unexpected exception.", exceptionCause);
  }

  private void publishWithHeader(String channelName, byte[] payload, UUID requestUniqueId) {
    try (StatefulRedisConnection<String, byte[]> borrow = connectionPool.borrowObject();
        PacketPacker packer = serdesContext.newPacketPacker()) {
      packer.packUUID(requestUniqueId);
      packer.packBinaryHeader(payload.length);
      packer.packPayload(payload);
      borrow
          .async()
          .publish(channelName, packer.toBinaryArray())
          .exceptionally(CompletableFutureUtils::delegateCaughtException);
    } catch (Exception exception) {
      throw new MessagePublicationException(
          "Could not publish message with attached request unique id as a header, because of unexpected exception.",
          exception);
    }
  }

  @VisibleForTesting
  void cancelTopicObservation(String channelName) {
    subscribedTopics.remove(channelName);
    subscribingConnection.sync().unsubscribe(channelName);
  }

  @Override
  public void close() {
    redisClient.close();
    connectionPool.close();
    subscribingConnection.close();
    subscribedTopics.clear();
  }

  @VisibleForTesting
  boolean whetherSubscriptionExists(String channelName) {
    return subscribedTopics.contains(channelName);
  }
}
