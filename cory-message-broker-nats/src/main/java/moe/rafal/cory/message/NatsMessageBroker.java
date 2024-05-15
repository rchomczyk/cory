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

import io.nats.client.Connection;
import io.nats.client.Message;
import java.util.concurrent.CompletableFuture;

class NatsMessageBroker implements MessageBroker {

  private final Connection connection;

  NatsMessageBroker(Connection connection) {
    this.connection = connection;
  }

  @Override
  public void publish(String channelName, byte[] payload) {
    connection.publish(channelName, payload);
  }

  @Override
  public void observe(String channelName, MessageListener listener) {
    connection
        .createDispatcher(
            message -> listener.receive(channelName, message.getReplyTo(), message.getData()))
        .subscribe(channelName);
  }

  @Override
  public CompletableFuture<byte[]> request(String channelName, byte[] payload) {
    return connection
        .request(channelName, payload)
        .thenApply(Message::getData);
  }

  @Override
  public void close() throws MessageBrokerClosingException {
    try {
      connection.close();
    } catch (InterruptedException exception) {
      throw new MessageBrokerClosingException(
          "Could not close message broker, because of unexpected exception.", exception);
    }
  }
}
