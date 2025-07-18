package cc.raynet.worldsharing.v1_17_1.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.utils.Utils;
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
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
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

    public MixinIntegratedServer(Thread lvt_1_1_, RegistryHolder lvt_2_1_, LevelStorageAccess lvt_3_1_, WorldData lvt_4_1_, PackRepository lvt_5_1_, Proxy lvt_6_1_, DataFixer lvt_7_1_, ServerResources lvt_8_1_, MinecraftSessionService lvt_9_1_, GameProfileRepository lvt_10_1_, GameProfileCache lvt_11_1_, ChunkProgressListenerFactory lvt_12_1_) {
        super(lvt_1_1_, lvt_2_1_, lvt_3_1_, lvt_4_1_, lvt_5_1_, lvt_6_1_, lvt_7_1_, lvt_8_1_, lvt_9_1_, lvt_10_1_, lvt_11_1_, lvt_12_1_);
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
            return Utils.channelInitConstructor.newInstance(listener);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
