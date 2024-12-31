package cc.raynet.worldsharing.v1_20_5.mixins;

import cc.raynet.worldsharing.api.APIHandler;
import cc.raynet.worldsharing.utils.VersionStorage;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Mixin(Connection.class)
public class MixinConnection {

    @Shadow
    private Channel channel;

    @Redirect(method = "connect", at = @At(value = "INVOKE", target = "Ljava/net/InetSocketAddress;getAddress()Ljava/net/InetAddress;"))
    private static InetAddress spoofHostname(InetSocketAddress instance) {
        return APIHandler.getClosestNode(instance.getAddress());
    }

    @Inject(method = "isMemoryConnection", at = @At("HEAD"), cancellable = true)
    public void change(CallbackInfoReturnable<Boolean> cir) {
        if (this.channel == null) {
            return;
        }
        final SocketAddress checkAddr = VersionStorage.proxyChannelAddress;
        if (checkAddr == null) {
            return;
        }
        if (checkAddr.equals(channel.localAddress()) || checkAddr.equals(channel.remoteAddress())) {
            cir.setReturnValue(false);
        }
    }
}
