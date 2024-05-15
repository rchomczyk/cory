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

package moe.rafal.cory.message;

import static java.util.UUID.randomUUID;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_TEST_PAYLOAD;

import java.io.IOException;
import java.util.UUID;
import moe.rafal.cory.serdes.MessagePackPacketPackerFactory;
import moe.rafal.cory.serdes.PacketPacker;

final class RedisMessageBrokerTestsUtils {

  private RedisMessageBrokerTestsUtils() {}

  static byte[] getPayloadWithRequestUniqueId(UUID requestUniqueId) throws IOException {
    try (PacketPacker packer = MessagePackPacketPackerFactory.INSTANCE.getPacketPacker()) {
      packer.packUUID(requestUniqueId);
      packer.packBinaryHeader(BROADCAST_TEST_PAYLOAD.length);
      packer.packPayload(BROADCAST_TEST_PAYLOAD);
      return packer.toBinaryArray();
    }
  }

  static byte[] getPayloadWithRequestUniqueId() throws IOException {
    return getPayloadWithRequestUniqueId(randomUUID());
  }
}
