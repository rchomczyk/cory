package moe.rafal.cory.packet.message;

public interface MessageBroker {

  void publish(byte[] channelName, byte[] payload);

  void observe(byte[] channelName, byte[] payload);
}
