package moe.rafal.cory.packet;

public class MalformedPacketException extends IllegalArgumentException {

  MalformedPacketException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
