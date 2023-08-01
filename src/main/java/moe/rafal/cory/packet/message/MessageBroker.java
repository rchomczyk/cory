package moe.rafal.cory.packet.message;

import java.io.Closeable;

public interface MessageBroker extends Closeable {

  void publish(String channelName, byte[] payload);

  void observe(String channelName, MessageListener listener);
}
