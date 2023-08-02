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

package moe.rafal.cory.packet;

import java.io.IOException;
import java.util.UUID;
import moe.rafal.cory.packet.serdes.PacketUnpacker;

interface PacketReader {

  <T extends Packet> T readPacket(PacketUnpacker unpacker)
      throws IOException, MalformedPacketException;

  <T extends Packet> Class<T> readPacketType(PacketUnpacker unpacker)
      throws IOException, MalformedPacketException;

  UUID readPacketUniqueId(PacketUnpacker unpacker)
      throws IOException;
}
