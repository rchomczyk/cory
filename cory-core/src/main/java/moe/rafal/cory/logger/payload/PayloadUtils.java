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

package moe.rafal.cory.logger.payload;

import java.util.HashSet;
import java.util.Set;
import moe.rafal.cory.Packet;
import moe.rafal.cory.serdes.PacketPacker;
import moe.rafal.cory.serdes.PacketSerdesContext;
import moe.rafal.cory.serdes.PacketUnpacker;

public final class PayloadUtils {

  private PayloadUtils() {}

  public static String getPacketPreview(
      final PacketSerdesContext serdesContext, final byte[] payload) {
    final Set<Object> elements = new HashSet<>();
    try (final PacketUnpacker unpacker = serdesContext.newPacketUnpacker(payload)) {
      while (unpacker.hasNext() && !unpacker.hasNextNilValue()) {
        elements.add(unpacker.unpackObject());
      }
    } catch (final Exception exception) {
      throw new PayloadConversionException(
          "Could not convert packet payload to preview, because of unexpected exception.",
          exception);
    }
    return elements.toString();
  }

  public static byte[] getPacketPayload(
      final PacketSerdesContext serdesContext, final Packet packet) {
    try (final PacketPacker packer = serdesContext.newPacketPacker()) {
      packet.write(packer);
      return packer.toBinaryArray();
    } catch (final Exception exception) {
      throw new PayloadConversionException(
          "Could not process convert payload to preview, because of unexpected exception.",
          exception);
    }
  }
}
