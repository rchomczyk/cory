package moe.rafal.cory.packet.message;

public interface MessageBroker {

  void publish(String channelName, byte[] payload);

  void observe(String channelName);
}
