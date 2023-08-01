package moe.rafal.cory.packet.message;

import static moe.rafal.cory.packet.message.MessageBrokerFactory.produceMessageBroker;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class MessageBrokerFactoryTests {

  private static final String INVALID_CONNECTION_URI = "nats://127.0.0.1:14322";
  private static final String INVALID_USERNAME = "shitzuu";
  private static final String INVALID_PASSWORD = "my-secret-password-123";
  private final MessageBrokerSpecification specification = new MessageBrokerSpecification(
      INVALID_CONNECTION_URI,
      INVALID_USERNAME,
      INVALID_PASSWORD);

  @Test
  void produceMessageBrokerThrowsWithoutServer() {
    // noinspection all
    assertThatCode(() -> produceMessageBroker(specification))
        .isInstanceOf(MessageBrokerInstantiationException.class);
  }
}
