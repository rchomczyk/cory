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

package moe.rafal.cory.serdes;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface PacketPacker extends Closeable {

  PacketPacker packArrayHeader(int value) throws IOException;

  <V> PacketPacker packArray(V[] value) throws IOException;

  PacketPacker packBinaryHeader(int value) throws IOException;

  PacketPacker packPayload(byte[] value) throws IOException;

  PacketPacker packString(String value) throws IOException;

  PacketPacker packBoolean(Boolean value) throws IOException;

  PacketPacker packInt(Integer value) throws IOException;

  PacketPacker packByte(Byte value) throws IOException;

  PacketPacker packLong(Long value) throws IOException;

  PacketPacker packUUID(UUID value) throws IOException;

  PacketPacker packShort(Short value) throws IOException;

  PacketPacker packFloat(Float value) throws IOException;

  PacketPacker packDouble(Double value) throws IOException;

  <K, V> PacketPacker packMap(Map<K, V> value) throws IOException;

  PacketPacker packMapHeader(int value) throws IOException;

  PacketPacker packInstant(Instant value) throws IOException;

  PacketPacker packDuration(Duration value) throws IOException;

  PacketPacker packEnum(Enum<?> value) throws IOException;

  <T> PacketPacker packAuto(T value) throws IOException;

  PacketPacker packNil() throws IOException;

  PacketPacker flush() throws IOException;

  byte[] toBinaryArray();
}
