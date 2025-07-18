package cc.raynet.worldsharing.protocol.pipeline;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketPrepender extends ByteToMessageDecoder {

    private final boolean selfRemove;

    public PacketPrepender() {
        this(false);
    }

    public PacketPrepender(boolean selfRemove) {
        this.selfRemove = selfRemove;
    }


    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> objects) {
        buffer.markReaderIndex();
        byte[] a = new byte[3];

        for (int i = 0; i < a.length; ++i) {
            if (!buffer.isReadable()) {
                buffer.resetReaderIndex();
                return;
            }

            a[i] = buffer.readByte();
            if (a[i] >= 0) {
                PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(a));

                try {
                    int varInt = buf.readVarIntFromBuffer();
                    if (buffer.readableBytes() >= varInt) {
                        objects.add(buffer.readBytes(varInt));
                        if (selfRemove) {
                            ctx.pipeline().remove(this);
                        }
                        return;
                    }

                    buffer.resetReaderIndex();
                } finally {
                    buf.buffer().release();
                }

                return;
            }
        }

        throw new RuntimeException("length wider than 21-bit");
    }
}
