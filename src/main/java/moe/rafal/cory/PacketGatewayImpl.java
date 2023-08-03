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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketUnpacker;

class PacketGatewayImpl implements PacketGateway {

  @Override
  public <T extends Packet> T readPacket(PacketUnpacker unpacker)
      throws IOException, MalformedPacketException {
    T packet = newPacketOf(readPacketType(unpacker));
    packet.setUniqueId(readPacketUniqueId(unpacker));
    packet.read(unpacker);
    return packet;
  }

  private <T extends Packet> T newPacketOf(Class<T> packetType) throws MalformedPacketException {
    try {
      return packetType.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
             InvocationTargetException exception) {
      throw new MalformedPacketException(
          "Packet could not be produced, because of missing public constructor without any parameters.",
          exception);
    }
  }

  @Override
  public <T extends Packet> Class<T> readPacketType(PacketUnpacker unpacker)
      throws IOException, MalformedPacketException {
    try {
      // noinspection unchecked
      return (Class<T>) Class.forName(unpacker.unpackString());
    } catch (ClassNotFoundException exception) {
      throw new MalformedPacketException(
          "Packet definition seems to be malformed, as packet type could not be found in classpath.",
          exception);
    }
  }

  @Override
  public UUID readPacketUniqueId(PacketUnpacker unpacker) throws IOException {
    return unpacker.unpackUUID();
  }

  @Override
  public <T extends Packet> void writePacket(T packet, PacketPacker packer)
      throws IOException {
    writePacketType(packet, packer);
    writePacketUniqueId(packet, packer);
    packet.write(packer);
  }

  @Override
  public <T extends Packet> void writePacketType(T packet, PacketPacker packer)
      throws IOException {
    packer.packString(packet.getClass().getName());
  }

  @Override
  public <T extends Packet> void writePacketUniqueId(T packet, PacketPacker packer)
      throws IOException {
    packer.packUUID(packet.getUniqueId());
  }
}
