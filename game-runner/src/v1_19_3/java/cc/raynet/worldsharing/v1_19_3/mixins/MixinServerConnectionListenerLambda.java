package cc.raynet.worldsharing.v1_19_3.mixins;

import cc.raynet.worldsharing.protocol.proxy.ChannelProxy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerConnectionListener$1")
public abstract class MixinServerConnectionListenerLambda extends ChannelInitializer<Channel> {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void storeClass(ServerConnectionListener this$0, CallbackInfo ci) throws NoSuchMethodException {
        if (ChannelProxy.channelInitConstructor == null) {
            ChannelProxy.channelInitConstructor = getClass().getDeclaredConstructor(ServerConnectionListener.class);
            ChannelProxy.channelInitConstructor.setAccessible(true);
        }
    }
}
