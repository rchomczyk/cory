package moe.rafal.example;

import static moe.rafal.cory.logger.LoggerFacade.getCoryLogger;
import static moe.rafal.cory.message.RedisMessageBrokerFactory.getRedisMessageBroker;

import io.lettuce.core.RedisURI;
import moe.rafal.cory.Cory;
import moe.rafal.cory.CoryBuilder;
import moe.rafal.cory.serdes.MessagePackPacketSerdesContext;

public class PongServer {

  public static void main(String[] args) {
    Cory cory =
        CoryBuilder.newBuilder()
            .withLoggerFacade(getCoryLogger(true))
            .withSerdesContext(MessagePackPacketSerdesContext.INSTANCE)
            .withMessageBroker(
                getRedisMessageBroker(
                    MessagePackPacketSerdesContext.INSTANCE,
                    RedisURI.create("redis://localhost:6379")))
            .build();
    cory.mutualObserve("ping-pong", new PongListener());

    // keep-alive
    while (true) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
    }
  }
}
