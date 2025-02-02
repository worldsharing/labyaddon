package cc.raynet.worldsharing.protocol.pipeline;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.protocol.model.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private final SessionHandler sessionHandler;

    public PacketEncoder(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) {
        int id = sessionHandler.getPacketRegistry().getPacketId(packet);

        if (id != 5 && id != 6) {
            WorldsharingAddon.LOGGER.debug("[CONTROL] [OUT] " + id + " " + packet.getClass()
                .getSimpleName());
        }
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeVarIntToBuffer(id);
        packet.write(buffer);
    }
}
