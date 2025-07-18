package cc.raynet.worldsharing.v1_12_2.mixins;

import cc.raynet.worldsharing.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.minecraft.network.NetworkSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.NetworkSystem$4")
public abstract class MixinNetworkSystemLambda extends ChannelInitializer<Channel> {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void storeClass(CallbackInfo ci) throws NoSuchMethodException {
        if (Utils.channelInitConstructor == null) {
            Utils.channelInitConstructor = getClass().getDeclaredConstructor(NetworkSystem.class);
            Utils.channelInitConstructor.setAccessible(true);
        }
    }
}
