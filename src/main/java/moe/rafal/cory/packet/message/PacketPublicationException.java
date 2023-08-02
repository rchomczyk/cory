package moe.rafal.cory.packet.message;

public class PacketPublicationException extends IllegalStateException {

  PacketPublicationException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
