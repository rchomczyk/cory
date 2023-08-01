package moe.rafal.cory.packet.message;

public interface MessageBroker<T, R> {

  void publish(T channelName, R payload);

  void observe(T channelName, R payload);
}
