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

import static java.lang.String.format;
import static moe.rafal.cory.serdes.PacketUnpackerFactory.producePacketUnpacker;

import java.io.IOException;
import moe.rafal.cory.serdes.PacketUnpacker;

class RedisMessageListener extends RedisMessageListenerDelegate<String, byte[]> {

  private final String subscribedTopic;
  private final MessageListener listener;

  RedisMessageListener(String subscribedTopic, MessageListener listener) {
    this.subscribedTopic = subscribedTopic;
    this.listener = listener;
  }

  @Override
  public void message(String channelName, byte[] message) {
    boolean whetherIsSubscribedTopic = subscribedTopic.equals(channelName);
    if (whetherIsSubscribedTopic) {
      processIncomingMessage(channelName, message);
    }
  }

  private void processIncomingMessage(String channelName, byte[] message) {
    try (PacketUnpacker unpacker = producePacketUnpacker(message)) {
      listener.receive(channelName,
          unpacker.unpackUUID().toString(),
          unpacker.unpackPayload());
    } catch (IOException exception) {
      throw new MessageProcessingException(
          "Could not process process incoming message with attached request unique id as a header, because of unexpected exception.",
          exception);
    }
  }

  @Override
  public void message(String pattern, String channelName, byte[] message) {
    message(format("%s:%s", pattern, channelName), message);
  }
}
