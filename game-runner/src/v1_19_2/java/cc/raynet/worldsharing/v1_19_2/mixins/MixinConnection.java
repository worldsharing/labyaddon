package cc.raynet.worldsharing.v1_19_2.mixins;

import cc.raynet.worldsharing.api.API;
import cc.raynet.worldsharing.utils.Utils;
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

    @Redirect(method = "connectToServer",
            at = @At(value = "INVOKE", target = "Ljava/net/InetSocketAddress;getAddress()Ljava/net/InetAddress;"))
    private static InetAddress spoofHostname(InetSocketAddress instance) {
        return API.getClosestNode(instance.getAddress());
    }

    @Inject(method = "isMemoryConnection", at = @At("HEAD"), cancellable = true)
    public void change(CallbackInfoReturnable<Boolean> cir) {
        if (this.channel == null) {
            return;
        }
        final SocketAddress checkAddr = Utils.proxyChannelAddress;
        if (checkAddr == null) {
            return;
        }
        if (checkAddr.equals(channel.localAddress()) || checkAddr.equals(channel.remoteAddress())) {
            cir.setReturnValue(false);
        }
    }
}
