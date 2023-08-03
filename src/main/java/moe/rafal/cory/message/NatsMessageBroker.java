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

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import moe.rafal.cory.jacoco.ExcludeFromJacocoGeneratedReport;

class NatsMessageBroker implements MessageBroker {

  private final Connection connection;

  NatsMessageBroker(MessageBrokerSpecification specification)
      throws IOException, InterruptedException {
    this.connection = Nats.connect(Options.builder()
        .server(specification.getConnectionUri())
        .userInfo(specification.getUsername(), specification.getPassword())
        .build());
  }

  @Override
  public void publish(String channelName, byte[] payload) {
    connection.publish(channelName, payload);
  }

  @Override
  public CompletableFuture<byte[]> request(String channelName, byte[] payload) {
    return connection
        .request(channelName, payload)
        .thenApply(Message::getData);
  }

  @Override
  public void observe(String channelName, MessageListener listener) {
    connection
        .createDispatcher(
            message -> listener.receive(channelName, message.getReplyTo(), message.getData()))
        .subscribe(channelName);
  }

  @ExcludeFromJacocoGeneratedReport
  @Override
  public void close() {
    try {
      connection.close();
    } catch (InterruptedException exception) {
      throw new RuntimeException(exception);
    }
  }
}
