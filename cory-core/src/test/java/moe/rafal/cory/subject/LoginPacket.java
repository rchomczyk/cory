/*
 *    Copyright 2023 cory
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

package moe.rafal.cory.subject;

import java.io.IOException;
import moe.rafal.cory.Packet;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;

public class LoginPacket extends Packet {

  private String username;
  private String password;

  public LoginPacket(String username, String password) {
    super();
    this.username = username;
    this.password = password;
  }

  public LoginPacket() {
    super();
  }

  @Override
  public void write(PacketPacker packer) throws IOException {
    packer.packString(username);
    packer.packString(password);
  }

  @Override
  public void read(PacketUnpacker unpacker) throws IOException {
    username = unpacker.unpackString();
    password = unpacker.unpackString();
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
