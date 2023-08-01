package moe.rafal.cory.packet.message;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;

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
  public void observe(String channelName) {
    connection.subscribe(channelName);
  }

  @Override
  public void close() {
    try {
      connection.close();
    } catch (InterruptedException exception) {
      throw new RuntimeException(exception);
    }
  }
}
