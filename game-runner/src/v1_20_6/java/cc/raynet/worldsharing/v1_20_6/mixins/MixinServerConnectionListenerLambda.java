package cc.raynet.worldsharing.v1_20_6.mixins;

import cc.raynet.worldsharing.utils.Utils;
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
        if (Utils.channelInitConstructor == null) {
            Utils.channelInitConstructor = getClass().getDeclaredConstructor(ServerConnectionListener.class);
            Utils.channelInitConstructor.setAccessible(true);
        }
    }
}
