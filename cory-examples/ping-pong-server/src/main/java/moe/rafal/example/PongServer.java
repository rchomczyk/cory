package moe.rafal.example;

import static moe.rafal.cory.logger.LoggerFacade.getCoryLogger;
import static moe.rafal.cory.message.RedisMessageBrokerFactory.getRedisMessageBroker;
import static moe.rafal.cory.serdes.MessagePackPacketSerdesContext.getMessagePackPacketSerdesContext;

import io.lettuce.core.RedisURI;
import moe.rafal.cory.Cory;
import moe.rafal.cory.CoryBuilder;
import moe.rafal.cory.serdes.PacketSerdesContext;

public class PongServer {

  public static void main(String[] args) {
    PacketSerdesContext serdesContext = getMessagePackPacketSerdesContext();

    Cory cory =
        CoryBuilder.newBuilder()
            .withLoggerFacade(getCoryLogger(serdesContext, true))
            .withSerdesContext(serdesContext)
            .withMessageBroker(
                getRedisMessageBroker(serdesContext, RedisURI.create("redis://localhost:6379")))
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
