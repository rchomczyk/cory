package moe.rafal.cory.packet.message;

import static moe.rafal.cory.packet.serdes.PacketPackerFactory.producePacketPacker;

import java.io.IOException;
import moe.rafal.cory.packet.Packet;
import moe.rafal.cory.packet.PacketGateway;
import moe.rafal.cory.packet.serdes.PacketPacker;

class PacketPublisherImpl implements PacketPublisher {

  private final PacketGateway packetGateway;
  private final MessageBroker messageBroker;

  PacketPublisherImpl(MessageBroker messageBroker) {
    this.packetGateway = PacketGateway.INSTANCE;
    this.messageBroker = messageBroker;
  }

  @Override
  public <T extends Packet> void publish(String channelName, T packet) {
    try (PacketPacker packer = producePacketPacker()) {
      packetGateway.writeDefinition(packet.getClass(), packer);
      packet.write(packer);
      messageBroker.publish(channelName, packer.toBinaryArray());
    } catch (IOException exception) {
      throw new PacketPublicationException(
          "Could not publish packet over the message broker, because of unexpected exception",
          exception);
    }
  }
}
