package moe.rafal.cory.packet.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageBrokerSpecificationTests {

  private static final String EXPECTED_CONNECTION_URI = "nats://127.0.0.1:4222";
  private static final String EXPECTED_USERNAME = "shitzuu";
  private static final String EXPECTED_PASSWORD = "my-secret-password-123-!@#";

  private final MessageBrokerSpecification specification = new MessageBrokerSpecification(
      EXPECTED_CONNECTION_URI,
      EXPECTED_USERNAME,
      EXPECTED_PASSWORD);

  @Test
  void getConnectionUriTest() {
    assertThat(specification.getConnectionUri())
        .isEqualTo(EXPECTED_CONNECTION_URI);
  }

  @Test
  void getUsernameTest() {
    assertThat(specification.getUsername())
        .isEqualTo(EXPECTED_USERNAME);
  }

  @Test
  void getPasswordTest() {
    assertThat(specification.getPassword())
        .isEqualTo(EXPECTED_PASSWORD);
  }
}
