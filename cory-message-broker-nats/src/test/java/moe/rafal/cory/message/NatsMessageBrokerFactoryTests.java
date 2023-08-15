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

import static moe.rafal.cory.message.NatsMessageBrokerFactory.produceNatsMessageBroker;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.nats.client.Options;
import org.junit.jupiter.api.Test;

class NatsMessageBrokerFactoryTests {

  private static final String INVALID_CONNECTION_URI = "nats://127.0.0.1:14322";
  private static final String INVALID_USERNAME = "shitzuu";
  private static final String INVALID_PASSWORD = "my-secret-password-123";

  @Test
  void produceNatsMessageBrokerThrowsWithoutServerTest() {
    assertThatCode(() -> produceNatsMessageBroker(Options.builder()
        .server(INVALID_CONNECTION_URI)
        .userInfo(INVALID_USERNAME, INVALID_PASSWORD)
        .build()))
        .isInstanceOf(MessageBrokerInstantiationException.class);
  }
}
