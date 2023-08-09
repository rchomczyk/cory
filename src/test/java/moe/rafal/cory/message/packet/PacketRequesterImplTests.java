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

import static java.lang.String.format;
import static moe.rafal.cory.PacketTestsUtils.BROADCAST_CHANNEL_NAME;
import static moe.rafal.cory.integration.EmbeddedNatsServerExtension.getNatsConnectionUri;
import static moe.rafal.cory.message.NatsMessageBrokerFactory.produceNatsMessageBroker;
import static moe.rafal.cory.message.packet.PacketRequesterFactory.producePacketRequester;
import static moe.rafal.cory.serdes.PacketPackerFactory.producePacketPacker;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import io.nats.client.Options;
import java.io.IOException;
import java.util.concurrent.CompletionException;
import moe.rafal.cory.Packet;
import moe.rafal.cory.PacketGateway;
import moe.rafal.cory.integration.EmbeddedNatsServerExtension;
import moe.rafal.cory.integration.InjectNatsServer;
import moe.rafal.cory.message.MessageBroker;
import moe.rafal.cory.serdes.PacketPacker;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmbeddedNatsServerExtension.class)
class PacketRequesterImplTests {

  @InjectNatsServer
  private EmbeddedNatsServer natsServer;
  private MessageBroker messageBroker;
  private PacketRequester packetRequester;

  @BeforeEach
  void createMessageBrokerAndPacketRequester() {
    messageBroker = produceNatsMessageBroker(Options.builder()
        .server(getNatsConnectionUri(natsServer))
        .build());
    packetRequester = producePacketRequester(messageBroker, PacketGateway.INSTANCE);
  }

  @Test
  void requestShouldThrowWhenWriteFails() throws IOException {
    Packet packetMock = mock(Packet.class);
    doThrow(new IOException())
        .when(packetMock)
        .write(any());
    assertThatCode(() -> packetRequester.request(BROADCAST_CHANNEL_NAME, packetMock))
        .isInstanceOf(PacketPublicationException.class)
        .hasMessage(
            "Could not request packet over the message broker, because of unexpected exception.");
  }

  @Test
  void requestShouldThrowWhenReadFails() throws IOException {
    PacketGateway packetGatewayMock = mock(PacketGateway.class);
    doThrow(new IOException())
        .when(packetGatewayMock)
        .readPacket(any());
    packetRequester = producePacketRequester(messageBroker, packetGatewayMock);
    try (PacketPacker packer = producePacketPacker()) {
      packer.packString("Hello");
      packer.packString("World");
      byte[] content = packer.toBinaryArray();
      assertThatCode(() -> packetRequester.processIncomingPacket(content).join())
          .isInstanceOf(CompletionException.class)
          .hasCauseInstanceOf(PacketProcessingException.class)
          .hasMessage(format("%s: %s",
              PacketProcessingException.class.getName(),
              "Could not complete processing of incoming request packet, because of unexpected exception."));
    }
  }
}
