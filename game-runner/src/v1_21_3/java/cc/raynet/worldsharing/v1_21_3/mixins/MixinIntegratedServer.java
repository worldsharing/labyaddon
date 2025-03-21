package cc.raynet.worldsharing.v1_21_3.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.proxy.ChannelProxy;
import cc.raynet.worldsharing.utils.Utils;
import com.mojang.datafixers.DataFixer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.net.SocketAddress;


@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer {

    public MixinIntegratedServer(Thread thread, LevelStorageAccess storage, PackRepository repo, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory factory) {
        super(thread, storage, repo, worldStem, proxy, dataFixer, services, factory);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    public void stopServer(CallbackInfo ci) {
        WorldsharingAddon.INSTANCE.sessionHandler.disconnect();
    }

    @Inject(method = "publishServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerConnectionListener;startTcpServerListener(Ljava/net/InetAddress;I)V",
            shift = At.Shift.AFTER))
    public void setProxyAddress(GameType gameType, boolean $$1, int $$2, CallbackInfoReturnable<Boolean> cir) {
        Utils.proxyChannelAddress = worldsharing$startProxyChannel(getConnection());
    }

    @Unique
    private SocketAddress worldsharing$startProxyChannel(ServerConnectionListener listener) {
        ChannelFuture ch = null;
        try {
            synchronized (listener.channels) {
                ch = new ServerBootstrap().channel(LocalServerChannel.class)
                        .childHandler(createChannelInitializer(listener))
                        .group(ServerConnectionListener.SERVER_EVENT_GROUP.get())
                        .localAddress(LocalAddress.ANY)
                        .bind()
                        .syncUninterruptibly();
                listener.channels.add(ch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ch == null ? null : ch.channel().localAddress();
    }

    private ChannelInitializer<Channel> createChannelInitializer(ServerConnectionListener listener) {
        try {
            return ChannelProxy.channelInitConstructor.newInstance(listener);
        } catch (ReflectiveOperationException e) {
            // TODO: UncheckedReflectiveOperationException when 1.20.4+ becomes the minimum
            throw new RuntimeException(e);
        }
    }

}
