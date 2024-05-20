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

package moe.rafal.cory.serdes;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface PacketUnpacker extends Closeable {

  void skipValue() throws IOException;

  int unpackArrayHeader() throws IOException;

  <V> V[] unpackArray() throws IOException;

  int unpackBinaryHeader() throws IOException;

  byte[] unpackPayload() throws IOException;

  String unpackString() throws IOException;

  Boolean unpackBoolean() throws IOException;

  Integer unpackInt() throws IOException;

  Byte unpackByte() throws IOException;

  Long unpackLong() throws IOException;

  UUID unpackUUID() throws IOException;

  Short unpackShort() throws IOException;

  Float unpackFloat() throws IOException;

  Double unpackDouble() throws IOException;

  int unpackMapHeader() throws IOException;

  <K, V> Map<K, V> unpackMap() throws IOException;

  Instant unpackInstant() throws IOException;

  Duration unpackDuration() throws IOException;

  <T extends Enum<T>> T unpackEnum() throws IOException;

  Object unpackObject() throws IOException;

  <T> T unpackAuto() throws IOException;

  boolean hasNext() throws IOException;

  boolean hasNextNilValue() throws IOException;
}
