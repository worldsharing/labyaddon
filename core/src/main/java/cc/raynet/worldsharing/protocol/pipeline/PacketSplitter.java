package cc.raynet.worldsharing.protocol.pipeline;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.types.VarInt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketSplitter extends MessageToByteEncoder<ByteBuf> {

    public PacketSplitter() {
    }

    protected void encode(ChannelHandlerContext ctx, ByteBuf inputBuffer, ByteBuf outputBuffer) {
        int inputLength = inputBuffer.readableBytes();
        int varIntSize = VarInt.size(inputLength);

        if (varIntSize > 3) {
            throw new IllegalArgumentException("unable to fit " + inputLength + " into 3 bytes");
        } else {
            outputBuffer.ensureWritable(varIntSize + inputLength);
            PacketBuffer.writeVarIntToBuffer(outputBuffer, inputLength);
            outputBuffer.writeBytes(inputBuffer, inputBuffer.readerIndex(), inputLength);
        }
    }
}

