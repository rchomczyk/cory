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


import io.lettuce.core.pubsub.RedisPubSubListener;
import java.io.IOException;
import java.util.UUID;
import moe.rafal.cory.serdes.PacketUnpacker;
import moe.rafal.cory.serdes.PacketUnpackerFactory;

class RedisMessageListener implements RedisPubSubListener<String, byte[]> {

  private final String channel;
  private final MessageListener listener;

  RedisMessageListener(String channel, MessageListener listener) {
    this.channel = channel;
    this.listener = listener;
  }

  @Override
  public void message(String channel, byte[] message) {
    if (!channel.equals(this.channel)) {
      return;
    }
    try (PacketUnpacker unpacker = PacketUnpackerFactory.producePacketUnpacker(message)) {
      UUID uuid = unpacker.unpackUUID();
      byte[] payload = unpacker.unpackPayload();
      listener.receive(channel, uuid.toString(), payload);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void message(String pattern, String channel, byte[] message) {
    message(String.format("%s:%s", pattern, channel), message);
  }

  @Override
  public void subscribed(String channel, long count) {

  }

  @Override
  public void psubscribed(String pattern, long count) {

  }

  @Override
  public void unsubscribed(String channel, long count) {

  }

  @Override
  public void punsubscribed(String pattern, long count) {

  }
}
