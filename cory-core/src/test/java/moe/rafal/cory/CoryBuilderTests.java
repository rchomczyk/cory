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

package moe.rafal.cory;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.MessageListener;
import org.junit.jupiter.api.Test;

class CoryBuilderTests {

  @Test
  void buildShouldThrowWithoutSpecifyingMessageBrokerTest() {
    assertThatCode(() -> CoryBuilder.newBuilder()
        .build())
        .isInstanceOf(CoryBuildException.class)
        .hasMessage("Cory could not be built, because of missing message broker, which is required for proper functioning.");
  }

  @Test
  void buildShouldThrowWithoutSpecifyingPacketPackerFactory() {
    assertThatCode(() -> CoryBuilder.newBuilder()
        .withMessageBroker(new MessageBroker() {
          @Override
          public void publish(String channelName, byte[] payload) {
          }

          @Override
          public void observe(String channelName, MessageListener listener) {
          }

          @Override
          public CompletableFuture<byte[]> request(String channelName, byte[] payload) {
            return null;
          }

          @Override
          public void close() {
          }
        })
        .withPacketUnpackerFactory(content -> null)
        .build())
        .isInstanceOf(CoryBuildException.class)
        .hasMessage("Cory could not be built, because of missing packet packer factory.");
  }

  @Test
  void buildShouldThrowWithoutSpecifyingPacketUnpackerFactory() {
    assertThatCode(() -> CoryBuilder.newBuilder()
        .withMessageBroker(new MessageBroker() {
          @Override
          public void publish(String channelName, byte[] payload) {
          }

          @Override
          public void observe(String channelName, MessageListener listener) {
          }

          @Override
          public CompletableFuture<byte[]> request(String channelName, byte[] payload) {
            return null;
          }

          @Override
          public void close() {
          }
        })
        .withPacketPackerFactory(() -> null)
        .build())
        .isInstanceOf(CoryBuildException.class)
        .hasMessage("Cory could not be built, because of missing packet unpacker factory.");
  }
}
