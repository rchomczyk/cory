package moe.rafal.example.proto.s2c;

import moe.rafal.cory.pojo.PojoPacket;

public class PongPacket extends PojoPacket {

  private String message;

  public PongPacket() {}

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
