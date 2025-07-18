package cc.raynet.worldsharing.v1_12_2.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.utils.Utils;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.GameType;
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

    public MixinMinecraftServer(File lvt_1_1_, Proxy lvt_2_1_, DataFixer lvt_3_1_, YggdrasilAuthenticationService lvt_4_1_, MinecraftSessionService lvt_5_1_, GameProfileRepository lvt_6_1_, PlayerProfileCache lvt_7_1_) {
        super(lvt_1_1_, lvt_2_1_, lvt_3_1_, lvt_4_1_, lvt_5_1_, lvt_6_1_, lvt_7_1_);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    public void stopServer(CallbackInfo ci) {
        WorldsharingAddon.INSTANCE.sessionHandler.disconnect();
    }

    @Inject(method = "shareToLAN", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkSystem;addEndpoint(Ljava/net/InetAddress;I)V", shift = Shift.AFTER))
    public void setIProxyInstance(GameType lvt_1_1_, boolean lvt_2_1_, CallbackInfoReturnable<String> cir) {
        Utils.proxyChannelAddress = worldsharing$startProxyChannel(getNetworkSystem());
    }

    @Unique
    private SocketAddress worldsharing$startProxyChannel(NetworkSystem listener) {
        ChannelFuture ch = null;
        try {
            synchronized (listener.endpoints) {
                ch = new ServerBootstrap().channel(LocalServerChannel.class)
                        .childHandler(createChannelInitializer(listener))
                        .group(NetworkSystem.SERVER_NIO_EVENTLOOP.getValue())
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
            return Utils.channelInitConstructor.newInstance(listener);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
