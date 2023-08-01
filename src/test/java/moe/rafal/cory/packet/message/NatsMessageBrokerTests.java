package moe.rafal.cory.packet.message;

import static moe.rafal.cory.packet.message.MessageBrokerFactory.produceMessageBroker;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import np.com.madanpokharel.embed.nats.EmbeddedNatsConfig;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(PER_CLASS)
class NatsMessageBrokerTests {

  private static final Duration MAXIMUM_RESPONSE_PERIOD = Duration.ofSeconds(2);
  private static final String EXPECTED_USERNAME = "shitzuu";
  private static final String EXPECTED_PASSWORD = "my-secret-password-123";
  private static final String EXPECTED_CHANNEL_NAME = "test-channel";
  private static final byte[] EXPECTED_MESSAGE_PAYLOAD = "Hello world".getBytes(
      StandardCharsets.UTF_8);
  private static final EmbeddedNatsServer EMBEDDED_SERVER = new EmbeddedNatsServer(
      EmbeddedNatsConfig.defaultNatsServerConfig());
  private static MessageBroker MESSAGE_BROKER;

  @BeforeAll
  static void startEmbeddedServerAndCreateMessageBroker() throws Exception {
    EMBEDDED_SERVER.startServer();
    MESSAGE_BROKER = produceMessageBroker(new MessageBrokerSpecification(
        getEmbeddedServerConnectionUri(),
        EXPECTED_USERNAME,
        EXPECTED_PASSWORD));
  }

  private static String getEmbeddedServerConnectionUri() {
    return String.format("nats://%s:%d",
        EMBEDDED_SERVER.getRunningHost(),
        EMBEDDED_SERVER.getRunningPort());
  }

  @Test
  void publishAndObserveTest() {
    AtomicBoolean receivedPayload = new AtomicBoolean();
    MESSAGE_BROKER.observe(EXPECTED_CHANNEL_NAME,
        (channelName, payload) -> receivedPayload.set(true));
    MESSAGE_BROKER.publish(EXPECTED_CHANNEL_NAME, EXPECTED_MESSAGE_PAYLOAD);
    await()
        .atMost(MAXIMUM_RESPONSE_PERIOD)
        .untilTrue(receivedPayload);
  }
}
