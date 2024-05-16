package moe.rafal.example;

import static moe.rafal.cory.logger.LoggerFacade.getCoryLogger;
import static moe.rafal.cory.message.RedisMessageBrokerFactory.getRedisMessageBroker;

import io.lettuce.core.RedisURI;
import moe.rafal.cory.Cory;
import moe.rafal.cory.CoryBuilder;
import moe.rafal.cory.serdes.MessagePackPacketSerdesContext;
import moe.rafal.example.proto.c2s.PingPacket;
import moe.rafal.example.proto.s2c.PongPacket;

public class PongClient {

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
    cory.<PingPacket, PongPacket>request("ping-pong", getPingPacket())
        .thenAccept(
            packet ->
                System.out.println(
                    "Received: " + packet.getUniqueId() + " - " + packet.getMessage()))
        .join();
  }

  private static PingPacket getPingPacket() {
    PingPacket pingPacket = new PingPacket();
    pingPacket.setMessage("Ping!");
    return pingPacket;
  }
}
