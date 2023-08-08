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

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketPackerFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

class RedisMessageBroker implements MessageBroker {

  private final RedisClient client;
  private final MessageBrokerSpecification specification;
  private final GenericObjectPool<StatefulRedisConnection<String, byte[]>> connectionPool;
  private final StatefulRedisPubSubConnection<String, byte[]> subConnection;
  private final Set<String> subscribedConnections = new HashSet<>();

  RedisMessageBroker(MessageBrokerSpecification specification) {
    this.specification = specification;
    this.client = RedisClient.create(RedisURI.create(specification.getConnectionUri()));
    this.connectionPool = ConnectionPoolSupport.createGenericObjectPool(
        () -> client.connect(new RedisDefaultCodec()), new GenericObjectPoolConfig<>());
    this.subConnection = this.client.connectPubSub(new RedisDefaultCodec());
  }

  @Override
  public void publish(String channelName, byte[] payload) {
    publishWithUuid(channelName, payload, UUID.randomUUID());
  }

  private void publishWithUuid(String channelName, byte[] payload, UUID uuid) {
    try (StatefulRedisConnection<String, byte[]> borrow = connectionPool.borrowObject()) {
      try (PacketPacker packer = PacketPackerFactory.producePacketPacker()) {
        packer.packUUID(uuid);
        packer.packBinaryHeader(payload.length);
        packer.packPayload(payload);
        borrow.sync().publish(channelName, packer.toBinaryArray());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void observe(String channelName, MessageListener listener) {
    subConnection.addListener(new RedisMessageListener(channelName, listener));
    subscribe(channelName);
  }

  @Override
  public CompletableFuture<byte[]> request(String channelName, byte[] payload) {
    UUID payloadUuid = UUID.randomUUID();
    CompletableFuture<byte[]> future = new CompletableFuture<byte[]>()
        .orTimeout(this.specification
            .getRequestCleanupInterval()
            .toSeconds(), TimeUnit.SECONDS)
        .exceptionally(throwable -> {
          unsubscribe(payloadUuid.toString());
          return null;
        });

    observe(payloadUuid.toString(),
        new RedisRequestMessageListener(payloadUuid.toString(), future));
    publishWithUuid(channelName, payload, payloadUuid);
    return future;
  }

  @Override
  public void close() throws IOException {
    this.client.close();
    this.subConnection.close();
    this.connectionPool.close();
  }

  private void subscribe(String channelName) {
    if (!subscribedConnections.contains(channelName)) {
      subscribedConnections.add(channelName);
      subConnection.async().subscribe(channelName);
    }
  }

  private void unsubscribe(String channelName) {
    if (subscribedConnections.contains(channelName)) {
      subscribedConnections.remove(channelName);
      subConnection.async().unsubscribe(channelName);
    }
  }
}
