package cc.raynet.worldsharing.protocol.pipeline;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.protocol.model.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    private final SessionHandler sessionHandler;

    public PacketDecoder(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() >= 1) {
            PacketBuffer buffer = new PacketBuffer(in);
            int id = buffer.readVarIntFromBuffer();
            Packet packet = this.sessionHandler.getPacketRegistry().getPacket(id);
            if (packet == null) {
                return;
            }
            if (id != 5 && id != 6) {
                WorldsharingAddon.LOGGER.debug("[CONTROL] [IN] " + id + " " + packet.getClass()
                        .getSimpleName());
            }

            packet.read(buffer);
            if (in.readableBytes() > 0) {
                WorldsharingAddon.LOGGER.error("Packet  (" + packet.getClass()
                        .getSimpleName() + ") was larger than expected, found " + in.readableBytes() + " bytes extra whilst reading packet " + packet);
            } else {
                out.add(packet);
            }
        }
    }

}
