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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.msgpack.core.MessageUnpacker;

class MessagePackPacketUnpacker implements PacketUnpacker {

  private final MessageUnpacker underlyingUnpacker;

  MessagePackPacketUnpacker(MessageUnpacker underlyingUnpacker) {
    this.underlyingUnpacker = underlyingUnpacker;
  }

  @Override
  public void skipValue() throws IOException {
    underlyingUnpacker.skipValue();
  }

  @Override
  public int unpackArrayHeader() throws IOException {
    return underlyingUnpacker.unpackArrayHeader();
  }

  @Override
  public int unpackBinaryHeader() throws IOException {
    return underlyingUnpacker.unpackBinaryHeader();
  }

  @Override
  public byte[] unpackPayload() throws IOException {
    return underlyingUnpacker.readPayload(unpackBinaryHeader());
  }

  @Override
  public String unpackString() throws IOException {
    return underlyingUnpacker.unpackString();
  }

  @Override
  public boolean unpackBoolean() throws IOException {
    return underlyingUnpacker.unpackBoolean();
  }

  @Override
  public int unpackInt() throws IOException {
    return underlyingUnpacker.unpackInt();
  }

  @Override
  public byte unpackByte() throws IOException {
    return underlyingUnpacker.unpackByte();
  }

  @Override
  public long unpackLong() throws IOException {
    return underlyingUnpacker.unpackLong();
  }

  @Override
  public UUID unpackUUID() throws IOException {
    return unpackMappingValue(UUID::fromString);
  }

  @Override
  public short unpackShort() throws IOException {
    return underlyingUnpacker.unpackShort();
  }

  @Override
  public float unpackFloat() throws IOException {
    return underlyingUnpacker.unpackFloat();
  }

  @Override
  public double unpackDouble() throws IOException {
    return underlyingUnpacker.unpackDouble();
  }

  @Override
  public int unpackMapHeader() throws IOException {
    return underlyingUnpacker.unpackMapHeader();
  }

  @Override
  public Instant unpackInstant() throws IOException {
    return unpackMappingValue(Instant::parse);
  }

  @Override
  public Duration unpackDuration() throws IOException {
    return unpackMappingValue(Duration::parse);
  }

  private <T> T unpackMappingValue(Function<String, T> valueMapper) throws IOException {
    return Optional.ofNullable(underlyingUnpacker.unpackString())
        .map(valueMapper)
        .orElse(null);
  }

  @Override
  public boolean hasNext() throws IOException {
    return underlyingUnpacker.hasNext();
  }

  @Override
  public void close() throws IOException {
    underlyingUnpacker.close();
  }
}
