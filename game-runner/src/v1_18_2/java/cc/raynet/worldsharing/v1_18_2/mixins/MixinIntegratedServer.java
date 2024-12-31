package cc.raynet.worldsharing.v1_18_2.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.proxy.ChannelProxy;
import cc.raynet.worldsharing.utils.VersionStorage;
import cc.raynet.worldsharing.v1_18_2.client.VersionBridgeImpl;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
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

    public MixinIntegratedServer(Thread $$0, LevelStorageAccess $$1, PackRepository $$2, WorldStem $$3, Proxy $$4, DataFixer $$5, MinecraftSessionService $$6, GameProfileRepository $$7, GameProfileCache $$8, ChunkProgressListenerFactory $$9) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7, $$8, $$9);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    public void stopServer(CallbackInfo ci) {
        WorldsharingAddon.INSTANCE.sessionHandler.disconnect();
    }

    @Inject(method = "publishServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConnectionListener;startTcpServerListener(Ljava/net/InetAddress;I)V", shift = At.Shift.AFTER))
    public void setProxyAddress(GameType gameType, boolean $$1, int $$2, CallbackInfoReturnable<Boolean> cir) {
        VersionStorage.proxyChannelAddress = worldsharing$startProxyChannel(getConnection());
    }

    @Inject(method = "initServer", at = @At("TAIL"))
    public void setMixinBridge(CallbackInfoReturnable<Boolean> cir) {
        VersionStorage.bridge = new VersionBridgeImpl();
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
