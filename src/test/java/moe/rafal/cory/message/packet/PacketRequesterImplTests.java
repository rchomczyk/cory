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

package moe.rafal.cory.message.packet;

import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_TEST_PAYLOAD;
import static moe.rafal.cory.integration.EmbeddedNatsServerExtension.getNatsConnectionUri;
import static moe.rafal.cory.message.MessageBrokerFactory.produceMessageBroker;
import static moe.rafal.cory.message.packet.PacketPublisherFactory.producePacketPublisher;
import static moe.rafal.cory.message.packet.PacketRequesterFactory.producePacketRequester;
import static moe.rafal.cory.serdes.PacketPackerFactory.producePacketPacker;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.integration.EmbeddedNatsServerExtension;
import moe.rafal.cory.integration.InjectNatsServer;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.message.MessageBrokerSpecification;
import moe.rafal.cory.serdes.PacketPacker;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedNatsServerExtension.class)
public class PacketRequesterImplTests {

  @InjectNatsServer
  private EmbeddedNatsServer natsServer;
  private MessageBroker messageBroker;
  private PacketRequester packetRequester;

  @BeforeEach
  void createMessageBrokerAndPacketRequester() {
    messageBroker = produceMessageBroker(new MessageBrokerSpecification(
        getNatsConnectionUri(natsServer), "", ""));
    packetRequester = producePacketRequester(messageBroker, PacketGateway.INSTANCE);
  }


  @Test
  void requestShouldThrowWhenWriteFails() throws IOException {
    Packet packetMock = mock(Packet.class);
    doThrow(new IOException())
        .when(packetMock)
        .write(any());
    assertThatCode(() -> packetRequester.request(BROADCAST_CHANNEL_NAME, packetMock, (ignored) -> {}))
        .isInstanceOf(PacketPublicationException.class);
  }

  @Test
  void requestShouldThrowWhenReadFails() throws IOException {
    try (PacketPacker packer = producePacketPacker()) {
      packer.packString("Hello");
      packer.packString("World");
      byte[] content = packer.toBinaryArray();
      assertThatCode(() -> packetRequester.handle(content, (ignored) -> {}))
          .isInstanceOf(PacketProcessingException.class);
    }
  }
}
