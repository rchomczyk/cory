package moe.rafal.cory.packet.message;

import java.io.IOException;

public final class MessageBrokerFactory {

  private MessageBrokerFactory() {

  }

  public static MessageBroker produceMessageBroker(MessageBrokerSpecification specification)
      throws MessageBrokerInstantiationException {
    try {
      return new NatsMessageBroker(specification);
    } catch (IOException | InterruptedException exception) {
      throw new MessageBrokerInstantiationException(
          "Could not instantiate message broker, because of unexpected exception.", exception);
    }
  }
}
