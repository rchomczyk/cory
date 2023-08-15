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

package moe.rafal.cory;

import static java.util.UUID.randomUUID;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;

public abstract class Packet {

  private UUID uniqueId;

  protected Packet(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  protected Packet() {
    this(randomUUID());
  }

  public abstract void write(PacketPacker packer) throws IOException;

  public abstract void read(PacketUnpacker unpacker) throws IOException;

  public UUID getUniqueId() {
    return uniqueId;
  }

  protected void setUniqueId(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public boolean equals(Object comparedObject) {
    if (this == comparedObject) {
      return true;
    }

    if (comparedObject == null || getClass() != comparedObject.getClass()) {
      return false;
    }

    Packet packet = (Packet) comparedObject;
    return Objects.equals(uniqueId, packet.uniqueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uniqueId);
  }
}
