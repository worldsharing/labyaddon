package cc.raynet.worldsharing.protocol.pipeline;

import cc.raynet.worldsharing.protocol.SessionHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

public class ChannelHandler extends ChannelInitializer<NioSocketChannel> {

    private final SessionHandler sessionHandler;
    private NioSocketChannel channel;

    public ChannelHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    protected void initChannel(NioSocketChannel channel) {
        this.channel = channel;
        channel.pipeline()
                .addLast("timeout", new ReadTimeoutHandler(30L, TimeUnit.SECONDS))
                .addLast("splitter", new PacketPrepender())
                .addLast("decoder", new PacketDecoder(this.sessionHandler))
                .addLast("prepender", new PacketSplitter())
                .addLast("encoder", new PacketEncoder())
                .addLast(this.sessionHandler);
    }

    public NioSocketChannel getChannel() {
        return this.channel;
    }

}
