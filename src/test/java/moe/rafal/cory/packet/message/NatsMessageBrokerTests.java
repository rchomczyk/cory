package moe.rafal.cory.packet.message;

import static moe.rafal.cory.packet.message.MessageBrokerFactory.produceMessageBroker;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import np.com.madanpokharel.embed.nats.EmbeddedNatsConfig;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NatsMessageBrokerTests {

  private static final Duration MAXIMUM_RESPONSE_PERIOD = Duration.ofSeconds(2);
  private static final String EXPECTED_USERNAME = "shitzuu";
  private static final String EXPECTED_PASSWORD = "my-secret-password-123";
  private static final String EXPECTED_CHANNEL_NAME = "test-channel";
  private static final byte[] EXPECTED_MESSAGE_PAYLOAD = "Hello world".getBytes(
      StandardCharsets.UTF_8);
  private final EmbeddedNatsServer embeddedServer = new EmbeddedNatsServer(
      EmbeddedNatsConfig.defaultNatsServerConfig());
  private MessageBroker messageBroker;

  @BeforeEach
  void startEmbeddedServerAndCreateMessageBroker() throws Exception {
    embeddedServer.startServer();
    messageBroker = produceMessageBroker(new MessageBrokerSpecification(
        getEmbeddedServerConnectionUri(),
        EXPECTED_USERNAME,
        EXPECTED_PASSWORD));
  }

  @AfterEach
  void ditchEmbeddedServer() {
    embeddedServer.stopServer();
  }

  private String getEmbeddedServerConnectionUri() {
    return String.format("nats://%s:%d",
        embeddedServer.getRunningHost(),
        embeddedServer.getRunningPort());
  }

  @Test
  void publishAndObserveTest() {
    AtomicBoolean receivedPayload = new AtomicBoolean();
    messageBroker.observe(EXPECTED_CHANNEL_NAME,
        (channelName, payload) -> receivedPayload.set(true));
    messageBroker.publish(EXPECTED_CHANNEL_NAME, EXPECTED_MESSAGE_PAYLOAD);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilTrue(receivedPayload);
  }

  @Test
  void closeTest() throws IOException {
    messageBroker.close();
    assertThatCode(() -> messageBroker.publish(EXPECTED_CHANNEL_NAME, EXPECTED_MESSAGE_PAYLOAD))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void closeShouldThrowWhenThreadIsOccupiedTest() throws IOException {
    MessageBroker messageBrokerMock = mock(MessageBroker.class);
    doThrow(new InterruptedException())
        .when(messageBrokerMock)
        .close();
    assertThatCode(messageBrokerMock::close)
        .isInstanceOf(InterruptedException.class);
  }
}
