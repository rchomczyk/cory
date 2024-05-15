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

import static moe.rafal.cory.message.RedisMessageBrokerFactory.getRedisMessageBroker;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisURI;
import moe.rafal.cory.serdes.MessagePackPacketPackerFactory;
import moe.rafal.cory.serdes.MessagePackPacketUnpackerFactory;
import org.junit.jupiter.api.Test;

class RedisMessageBrokerFactoryTests {

  private static final String INVALID_CONNECTION_URI = "redis://127.0.0.1:11647";

  @Test
  void getRedisMessageBrokerThrowsWithoutServerTest() {
    assertThatCode(
            () ->
                getRedisMessageBroker(
                    MessagePackPacketPackerFactory.INSTANCE,
                    MessagePackPacketUnpackerFactory.INSTANCE,
                    RedisURI.create(INVALID_CONNECTION_URI)))
        .isInstanceOf(RedisConnectionException.class)
        .hasMessageStartingWith("Unable to connect to ");
  }
}
