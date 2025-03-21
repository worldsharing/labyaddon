package cc.raynet.worldsharing.v1_12_2.mixins;

import cc.raynet.worldsharing.api.API;
import cc.raynet.worldsharing.utils.Utils;
import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.SocketAddress;

@Mixin(NetworkManager.class)
public class MixinConnection {

    @Shadow
    private Channel channel;

    @ModifyArg(method = "createNetworkManagerAndConnect", at = @At(value = "INVOKE",
            target = "Lio/netty/bootstrap/Bootstrap;connect(Ljava/net/InetAddress;I)Lio/netty/channel/ChannelFuture;"),
            index = 0)
    private static InetAddress spoofHostname(InetAddress inetHost) {
        return API.getClosestNode(inetHost);
    }

    @Inject(method = "isLocalChannel", at = @At("HEAD"), cancellable = true)
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