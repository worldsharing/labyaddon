package cc.raynet.worldsharing.protocol.pipeline;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.types.ID;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeVarIntToBuffer(packet.getID());

        if (packet.getID() != ID.PING.value && packet.getID() != ID.PONG.value) {
            WorldsharingAddon.LOGGER.debug("[CONTROL] [OUT] " + packet.getID() + " " + packet.getClass()
                    .getSimpleName());
        }
        packet.write(buffer);
    }
}
