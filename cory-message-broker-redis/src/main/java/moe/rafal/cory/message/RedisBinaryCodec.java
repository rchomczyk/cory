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

package moe.rafal.cory.message;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.lettuce.core.codec.RedisCodec;
import java.nio.ByteBuffer;

class RedisBinaryCodec implements RedisCodec<String, byte[]> {

  RedisBinaryCodec() {}

  @Override
  public String decodeKey(ByteBuffer buffer) {
    return UTF_8.decode(buffer).toString();
  }

  @Override
  public byte[] decodeValue(ByteBuffer buffer) {
    byte[] array = new byte[buffer.remaining()];
    buffer.get(array);
    return array;
  }

  @Override
  public ByteBuffer encodeKey(String value) {
    return ByteBuffer.wrap(value.getBytes(UTF_8));
  }

  @Override
  public ByteBuffer encodeValue(byte[] value) {
    return ByteBuffer.wrap(value);
  }
}
