package cc.raynet.worldsharing.v1_21_4.mixins;

import cc.raynet.worldsharing.api.APIHandler;
import cc.raynet.worldsharing.utils.VersionStorage;
import cc.raynet.worldsharing.v1_21_4.client.PropertyStorage;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Mixin(Connection.class)
public class MixinConnection implements PropertyStorage {

    @Unique
    private Property[] worldsharing$properties;

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

    @Override
    public Property[] worldsharing$getProperties() {
        return worldsharing$properties;
    }

    @Override
    public void worldsharing$setProperties(Property[] properties) {
        this.worldsharing$properties = properties;
    }
}
