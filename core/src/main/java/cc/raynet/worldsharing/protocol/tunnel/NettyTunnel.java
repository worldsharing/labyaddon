package cc.raynet.worldsharing.protocol.tunnel;

import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.protocol.packets.PacketTunnelRequest;
import cc.raynet.worldsharing.protocol.pipeline.PacketPrepender;
import cc.raynet.worldsharing.utils.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.incubator.codec.quic.*;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NettyTunnel extends AbstractTunnel {

    private static NioEventLoopGroup group = null;

    private volatile QuicChannel quicChannel; // volatile?
    private volatile Channel datagramChannel;


    public NettyTunnel(SessionHandler sessionHandler, PacketTunnelRequest t) {
        super(sessionHandler, t, Type.NETTY);

        QuicSslContext context = QuicSslContextBuilder.forClient()
                .applicationProtocols(applicationProtocol)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        ChannelHandler codec = new QuicClientCodecBuilder().sslContext(context)
                .maxIdleTimeout(10, TimeUnit.SECONDS)
                .initialMaxData(4611686018427387903L)
                .initialMaxStreamDataBidirectionalRemote(1250000)
                .initialMaxStreamDataBidirectionalLocal(1250000)
                .initialMaxStreamDataUnidirectional(1250000)
                .initialMaxStreamsBidirectional(512)
                .build();

        if (group == null) {
            group = new NioEventLoopGroup();
        }

        new Bootstrap().group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .bind(0)
                .addListener((ChannelFuture datagramFuture) -> {
                    if (!datagramFuture.isSuccess()) {
                        logError("Failed to bind Datagram channel", datagramFuture.cause());
                        return;
                    }
                    datagramChannel = datagramFuture.channel();

                    QuicChannel.newBootstrap(datagramChannel)
                            .streamHandler(new ChannelInitializer<QuicStreamChannel>() {
                                @Override
                                protected void initChannel(QuicStreamChannel channel) {
                                    channel.pipeline().addLast(new PacketPrepender(true));
                                    channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            super.userEventTriggered(ctx, evt);
                                            if (evt instanceof ChannelInputShutdownEvent || evt instanceof ChannelInputShutdownReadComplete) {
                                                ctx.close();
                                            }
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            super.channelInactive(ctx);
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                            logError("Stream error", cause);
                                            ctx.close();
                                        }
                                    });
                                    channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
                                            channel.pipeline().remove(this);

                                            Runnable closeCallback = handleMetadata(buf);

                                            CompletableFuture<Channel> future = new CompletableFuture<>();
                                            sessionHandler.addon.manager().openChannel(null, future);
                                            Channel peer = future.get();

                                            peer.closeFuture().addListener(f -> safeClose(channel, null));
                                            channel.closeFuture().addListener(f -> safeClose(peer, closeCallback));

                                            peer.pipeline().addLast(new ChannelProxy(channel));
                                            channel.pipeline().addLast(new ChannelProxy(peer));

                                        }
                                    });

                                }
                            })
                            .handler(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    super.exceptionCaught(ctx, cause);
                                    logError("QUIC channel exception", cause);
                                    ctx.close();
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelInactive(ctx);
                                }
                            })
                            .remoteAddress(Utils.splitHostAndPort(t.target))
                            .connect()
                            .addListener(quicFuture -> {
                                if (!quicFuture.isSuccess()) {
                                    logError("Failed to connect to QUIC target", quicFuture.cause());
                                    return;
                                }
                                quicChannel = (QuicChannel) quicFuture.get();

                                quicChannel.createStream(QuicStreamType.BIDIRECTIONAL, new ChannelInitializer<QuicStreamChannel>() {
                                    @Override
                                    protected void initChannel(QuicStreamChannel ch) throws Exception {
                                        ch.writeAndFlush(ch.alloc().buffer().writeBytes(getPayload()))
                                                .addListener(future -> {
                                                    if (!future.isSuccess()) {
                                                        logError("Failed to send handshake", future.cause());
                                                        ch.close();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        logError("Handshake stream exception", cause);
                                        ctx.close();
                                    }
                                });

                            });
                });
    }

    private static void logError(String message, Throwable cause) {
        System.err.println("[NettyTunnel] ERROR: " + message);
        if (cause != null) cause.printStackTrace();
    }

    private static void safeClose(Channel channel, Runnable optionalCallback) {
        if (channel != null && channel.isOpen()) {
            channel.close().addListener(f -> {
                if (optionalCallback != null) optionalCallback.run();
            });
        } else {
            if (optionalCallback != null) optionalCallback.run();
        }
    }

    public static void shutdown() {
        if (group == null) return;
        group.shutdownGracefully();
        group = null;
    }

    @Override
    public void close() {
        if (quicChannel != null && quicChannel.isOpen()) {
            quicChannel.close();
            quicChannel = null;
        }
        if (datagramChannel != null && datagramChannel.isOpen()) {
            datagramChannel.close();
            datagramChannel = null;
        }
    }

    private static class ChannelProxy extends ChannelInboundHandlerAdapter {

        private final Channel peer;

        public ChannelProxy(Channel peer) {
            this.peer = peer;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.read();
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (peer.isActive()) {
                peer.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                });
            } else {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (peer.isActive()) {
                peer.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
            peer.close();
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            if (ctx.channel().isActive()) {
                ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
            peer.close();
            ctx.close();
        }
    }

}
