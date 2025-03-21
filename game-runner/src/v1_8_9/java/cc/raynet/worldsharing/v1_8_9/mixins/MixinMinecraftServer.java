package cc.raynet.worldsharing.v1_8_9.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.proxy.ChannelProxy;
import cc.raynet.worldsharing.utils.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.net.Proxy;
import java.net.SocketAddress;

@Mixin(IntegratedServer.class)
public abstract class MixinMinecraftServer extends MinecraftServer {

    public MixinMinecraftServer(Proxy lvt_1_1_, File lvt_2_1_) {
        super(lvt_1_1_, lvt_2_1_);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    public void stopServer(CallbackInfo ci) {
        WorldsharingAddon.INSTANCE.sessionHandler.disconnect();
    }

    @Inject(method = "shareToLAN", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkSystem;addLanEndpoint(Ljava/net/InetAddress;I)V",
            shift = Shift.AFTER))
    public void setIProxyInstance(WorldSettings.GameType par1, boolean par2, CallbackInfoReturnable<String> cir) {
        Utils.proxyChannelAddress = worldsharing$startProxyChannel(getNetworkSystem());
    }

    @Unique
    private SocketAddress worldsharing$startProxyChannel(NetworkSystem listener) {
        ChannelFuture ch = null;
        try {
            synchronized (listener.endpoints) {
                ch = new ServerBootstrap().channel(LocalServerChannel.class)
                        .childHandler(createChannelInitializer(listener))
                        .group(NetworkSystem.eventLoops.getValue())
                        .localAddress(LocalAddress.ANY)
                        .bind()
                        .syncUninterruptibly();
                listener.endpoints.add(ch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ch == null ? null : ch.channel().localAddress();
    }

    private ChannelInitializer<Channel> createChannelInitializer(NetworkSystem listener) {
        try {
            return ChannelProxy.channelInitConstructor.newInstance(listener);
        } catch (ReflectiveOperationException e) {
            // TODO: UncheckedReflectiveOperationException when 1.20.4+ becomes the minimum
            throw new RuntimeException(e);
        }
    }

}
