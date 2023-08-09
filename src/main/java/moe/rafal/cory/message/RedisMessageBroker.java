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

import static io.lettuce.core.support.ConnectionPoolSupport.createGenericObjectPool;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static moe.rafal.cory.serdes.PacketPackerFactory.producePacketPacker;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.serdes.PacketPacker;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

class RedisMessageBroker implements MessageBroker {

  private static final RedisCodec<String, byte[]> DEFAULT_CODEC = new RedisBinaryCodec();
  private final MessageBrokerSpecification specification;
  private final RedisClient redisClient;
  private final GenericObjectPool<StatefulRedisConnection<String, byte[]>> connectionPool;
  private final StatefulRedisPubSubConnection<String, byte[]> subscribingConnection;
  private final Set<String> subscribedTopics;

  RedisMessageBroker(MessageBrokerSpecification specification) {
    this.specification = specification;
    this.redisClient = RedisClient.create(RedisURI.create(specification.getConnectionUri()));
    this.connectionPool = getRedisConnectionPool();
    this.subscribingConnection = this.redisClient.connectPubSub(DEFAULT_CODEC);
    this.subscribedTopics = new HashSet<>();
  }

  private GenericObjectPool<StatefulRedisConnection<String, byte[]>> getRedisConnectionPool() {
    return createGenericObjectPool(() -> redisClient.connect(DEFAULT_CODEC),
        new GenericObjectPoolConfig<>());
  }

  @Override
  public void publish(String channelName, byte[] payload) {
    publishWithHeader(channelName, payload, randomUUID());
  }

  @Override
  public void observe(String channelName, MessageListener listener) {
    subscribingConnection.addListener(new RedisMessageListener(channelName, listener));
    beginTopicObservation(channelName);
  }

  private void beginTopicObservation(String channelName) {
    if (whetherSubscriptionExists(channelName)) {
      return;
    }
    subscribedTopics.add(channelName);
    subscribingConnection.sync().subscribe(channelName);
  }

  private boolean whetherSubscriptionExists(String channelName) {
    return subscribedTopics.contains(channelName);
  }

  @Override
  public CompletableFuture<byte[]> request(String channelName, byte[] payload) {
    UUID payloadUniqueId = randomUUID();

    CompletableFuture<byte[]> promisedResponse = new CompletableFuture<byte[]>()
        .orTimeout(specification.getRequestCleanupInterval().toSeconds(), SECONDS)
        .exceptionally(exception -> {
          cancelTopicObservation(payloadUniqueId.toString());
          return null;
        });

    observe(payloadUniqueId.toString(),
        new RedisRequestMessageListener(payloadUniqueId.toString(), promisedResponse));
    publishWithHeader(channelName, payload, payloadUniqueId);
    return promisedResponse;
  }

  private void publishWithHeader(String channelName, byte[] payload, UUID requestUniqueId) {
    try (StatefulRedisConnection<String, byte[]> borrow = connectionPool.borrowObject();
        PacketPacker packer = producePacketPacker()) {
      packer.packUUID(requestUniqueId);
      packer.packBinaryHeader(payload.length);
      packer.packPayload(payload);
      borrow.sync().publish(channelName, packer.toBinaryArray());
    } catch (Exception exception) {
      throw new MessagePublicationException(
          "Could not publish message with attached request unique id as a header, because of unexpected exception.",
          exception);
    }
  }

  private void cancelTopicObservation(String channelName) {
    if (whetherSubscriptionExists(channelName)) {
      subscribedTopics.remove(channelName);
      subscribingConnection.sync().unsubscribe(channelName);
    }
  }

  @Override
  public void close() throws IOException {
    this.redisClient.close();
    this.connectionPool.close();
    this.subscribingConnection.close();
    this.subscribedTopics.clear();
  }
}
