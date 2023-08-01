package moe.rafal.cory.packet.message;

@FunctionalInterface
public interface MessageListener {

  void receive(String channelName, byte[] payload);
}
