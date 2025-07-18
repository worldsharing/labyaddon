package cc.raynet.worldsharing.protocol.tunnel;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.protocol.packets.PacketTunnelRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import tech.kwik.core.QuicClientConnection;
import tech.kwik.core.QuicStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.time.Duration;

public class KwikTunnel extends AbstractTunnel {

    private final QuicClientConnection connection;

    KwikTunnel(SessionHandler sessionHandler, PacketTunnelRequest tunnelRequest) throws Exception {
        super(sessionHandler, tunnelRequest, Type.KWIK);

        connection = QuicClientConnection.newBuilder()
                .uri(URI.create("udp://" + tunnelRequest.target))
                .applicationProtocol(applicationProtocol)
                .noServerCertificateCheck()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        connection.connect();
        connection.keepAlive(20);

        QuicStream authStream = connection.createStream(true);
        authStream.getOutputStream().write(getPayload());
        authStream.getOutputStream().flush();

        connection.setPeerInitiatedStreamCallback(stream -> {
            try {
                InputStream inputStream = stream.getInputStream();

                int length = PacketBuffer.readVarIntFromStream(inputStream);
                ByteBuf buf = Unpooled.wrappedBuffer(inputStream.readNBytes(length));

                Runnable closeCallback = handleMetadata(buf);

                sessionHandler.addon.manager().openChannel(peer -> peer.pipeline().addLast("handler", new ChannelProxy(stream, closeCallback)), null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    void close() {
        connection.close();
    }

    private static class ChannelProxy extends ChannelInboundHandlerAdapter {

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
                } catch (IOException ignored) {
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
                } catch (IOException ignored) {
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
}
