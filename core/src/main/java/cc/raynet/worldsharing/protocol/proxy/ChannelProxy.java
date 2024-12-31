package cc.raynet.worldsharing.protocol.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import net.luminis.quic.QuicStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

public class ChannelProxy extends ChannelInboundHandlerAdapter {

    // Credits: https://github.com/Gaming32/world-host/
    public static Constructor<? extends ChannelInitializer<Channel>> channelInitConstructor = null;

    private final InputStream dataInputStream;
    private final OutputStream dataOutputStream;
    private final Runnable closeCallback;

    public ChannelProxy(QuicStream stream, Runnable closeCallback) {
        this.dataInputStream = stream.getInputStream();
        this.dataOutputStream = stream.getOutputStream();
        this.closeCallback = closeCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Thread.ofVirtual().start(() -> {
            try {
                while (true) {
                    int data = dataInputStream.read();
                    if (data == -1) {
                        break;
                    }

                    ctx.writeAndFlush(ctx.alloc().buffer(1).writeByte(data));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ctx.close();
                closeCallback.run();
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf buf) {
            try {
                while (buf.isReadable()) {
                    dataOutputStream.write(buf.readByte());
                }
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        closeCallback.run();
    }
}
