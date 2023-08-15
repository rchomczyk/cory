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
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_TEST_PAYLOAD;
import static moe.rafal.cory.message.RedisMessageBrokerTestsUtils.getPayloadWithRequestUniqueId;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import moe.rafal.cory.serdes.MessagePackPacketUnpackerFactory;
import org.junit.jupiter.api.Test;

class RedisMessageListenerTests {

  private static final String EXPECTED_CHANNEL_NAME = "test-channel-name";
  private static final String EXPECTED_CHANNEL_PATTERN = "test-pattern";

  @Test
  void verifyWhetherMessageProxiesCallTest() {
    RedisMessageListener redisMessageListenerMock = mock(RedisMessageListener.class);
    doCallRealMethod()
        .when(redisMessageListenerMock)
        .message(any(), any(), any());
    redisMessageListenerMock.message(EXPECTED_CHANNEL_PATTERN, EXPECTED_CHANNEL_NAME,
        BROADCAST_TEST_PAYLOAD);
    verify(redisMessageListenerMock)
        .message(format("%s:%s", EXPECTED_CHANNEL_PATTERN, EXPECTED_CHANNEL_NAME),
            BROADCAST_TEST_PAYLOAD);
  }

  @Test
  void verifyWhetherExceptionIsThrownByProcessIncomingMessageTest() {
    MessageListener messageListenerMock = mock(MessageListener.class);
    doAnswer(invocationOnMock -> {
      throw new IOException();
    })
        .when(messageListenerMock)
        .receive(any(), any(), any());
    RedisMessageListener redisMessageListener = new RedisMessageListener(
        MessagePackPacketUnpackerFactory.INSTANCE, BROADCAST_CHANNEL_NAME, messageListenerMock);
    assertThatCode(() -> redisMessageListener.processIncomingMessage(BROADCAST_CHANNEL_NAME,
        getPayloadWithRequestUniqueId()))
        .isInstanceOf(MessageProcessingException.class)
        .hasMessage(
            "Could not process process incoming message with attached request unique id as a header, because of unexpected exception.");
  }
}
