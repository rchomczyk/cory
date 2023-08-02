package moe.rafal.cory.packet;

public class PacketMalformedDefinitionException extends IllegalArgumentException {

  PacketMalformedDefinitionException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
