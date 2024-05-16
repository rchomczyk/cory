/*
 *    Copyright 2023-2024 cory
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package moe.rafal.example;

import moe.rafal.cory.message.packet.PacketListenerDelegate;
import moe.rafal.example.proto.c2s.PingPacket;
import moe.rafal.example.proto.s2c.PongPacket;

public class PongListener extends PacketListenerDelegate<PingPacket> {

  protected PongListener() {
    super(PingPacket.class, true);
  }

  private static PongPacket getPongPacket(final PingPacket request) {
    PongPacket pongPacket = new PongPacket();
    pongPacket.setMessage(request.getMessage() + " Pong!");
    return pongPacket;
  }

  @Override
  public PongPacket process(
      final String channelName, final String replyChannelName, final PingPacket packet) {
    return getPongPacket(packet);
  }
}
