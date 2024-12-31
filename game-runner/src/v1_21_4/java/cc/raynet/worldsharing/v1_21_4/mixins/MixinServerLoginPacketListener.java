package cc.raynet.worldsharing.v1_21_4.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLoginPacketListenerImpl.class)
public class MixinServerLoginPacketListener {

    @Shadow
    String requestedUsername;

    @Redirect(method = "handleHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;usesAuthentication()Z"))
    public boolean usesAuthentication(MinecraftServer instance) {
        return instance.usesAuthentication() && !WorldsharingAddon.INSTANCE.bedrockPlayers.remove(requestedUsername);

    }
}
