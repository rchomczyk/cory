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

import static java.time.Duration.ofSeconds;

import io.lettuce.core.RedisURI;
import java.time.Duration;
import moe.rafal.cory.serdes.PacketSerdesContext;

public final class RedisMessageBrokerFactory {

  private static final Duration DEFAULT_REQUEST_CLEANUP_INTERVAL = ofSeconds(5);

  private RedisMessageBrokerFactory() {}

  public static MessageBroker getRedisMessageBroker(
      PacketSerdesContext serdesContext, RedisURI redisUri, Duration requestCleanupInterval) {
    return new RedisMessageBroker(serdesContext, redisUri, requestCleanupInterval);
  }

  public static MessageBroker getRedisMessageBroker(
      PacketSerdesContext serdesContext, RedisURI redisUri) {
    return getRedisMessageBroker(serdesContext, redisUri, DEFAULT_REQUEST_CLEANUP_INTERVAL);
  }
}
