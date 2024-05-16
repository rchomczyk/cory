package moe.rafal.example.proto.c2s;

import moe.rafal.cory.pojo.PojoPacket;

public class PingPacket extends PojoPacket {

  private String message;

  public PingPacket() {}

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
