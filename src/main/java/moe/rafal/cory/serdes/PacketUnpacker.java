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
import java.util.UUID;

public interface PacketUnpacker extends Closeable {

  void skipValue() throws IOException;

  int unpackArrayHeader() throws IOException;

  int unpackBinaryHeader() throws IOException;

  byte[] unpackPayload() throws IOException;

  String unpackString() throws IOException;

  boolean unpackBoolean() throws IOException;

  int unpackInt() throws IOException;

  byte unpackByte() throws IOException;

  long unpackLong() throws IOException;

  UUID unpackUUID() throws IOException;

  short unpackShort() throws IOException;

  float unpackFloat() throws IOException;

  double unpackDouble() throws IOException;

  int unpackMapHeader() throws IOException;

  Instant unpackInstant() throws IOException;

  Duration unpackDuration() throws IOException;

  boolean hasNext() throws IOException;
}
