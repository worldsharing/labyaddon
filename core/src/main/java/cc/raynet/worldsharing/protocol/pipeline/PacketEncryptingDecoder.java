package cc.raynet.worldsharing.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import java.util.List;

public class PacketEncryptingDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final EncryptionTranslator codec;

    public PacketEncryptingDecoder(Cipher cipher) {
        codec = new EncryptionTranslator(cipher);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        list.add(codec.decipher(channelHandlerContext, byteBuf));
    }
}
