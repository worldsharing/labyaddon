package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.packets.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;

@Sharable
public abstract class PacketHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        this.handlePacket((Packet) object);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            WorldsharingAddon.INSTANCE.sessionHandler.lastError = cause.getMessage();
            WorldsharingAddon.INSTANCE.sessionHandler.disconnect();
        }
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        WorldsharingAddon.INSTANCE.sessionHandler.disconnect("Connection closed");
        super.channelInactive(ctx);
    }


    protected void handlePacket(Packet packet) {
        packet.handle(this);
    }

    public abstract void handle(PacketAuthRequest enc);

    public abstract void handle(PacketTunnelRequest tunnelRequest);

    public abstract void handle(PacketTunnelInfo ti);

    public abstract void handle(PacketError err);

    public abstract void handle(PacketPing ping);

    public abstract void handle(PacketDisconnect disconnect);

    public abstract void handle(PacketReady ready);

    public abstract void handle(PacketWhitelist whitelist);
}
