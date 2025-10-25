package cc.raynet.worldsharing.v1_21_10.mixins;

import cc.raynet.worldsharing.v1_21_10.PropertyStorage;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinServerLoginPacketListener {

    @Shadow
    @Final
    Connection connection;

    @Shadow
    abstract void startClientVerification(GameProfile $$0);

    @Redirect(method = "handleHello",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;usesAuthentication()Z"))
    public boolean usesAuthentication(MinecraftServer instance) {
        return instance.usesAuthentication() && ((PropertyStorage) connection).worldsharing$getProperties() == null;
    }

    @Inject(method = "handleHello", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V",
            ordinal = 1), cancellable = true)
    public void setId(ServerboundHelloPacket helloPacket, CallbackInfo ci) {
        GameProfile profile = new GameProfile(helloPacket.profileId(), helloPacket.name());
        Property[] properties = ((PropertyStorage) connection).worldsharing$getProperties();

        if (properties != null) {
            for (var map : properties) {
                profile.properties().put(map.name(), map);
            }
        }

        startClientVerification(profile);
        ci.cancel();
    }
}
