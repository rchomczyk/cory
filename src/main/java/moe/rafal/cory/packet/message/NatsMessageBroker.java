package moe.rafal.cory.packet.message;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;

class NatsMessageBroker implements MessageBroker {

  private final Connection connection;

  NatsMessageBroker(Options.Builder options) throws IOException, InterruptedException {
    this.connection = Nats.connect(options.build());
  }

  @Override
  public void publish(String channelName, byte[] payload) {
    connection.publish(channelName, payload);
  }

  @Override
  public void observe(String channelName) {
    connection.subscribe(channelName);
  }
}
