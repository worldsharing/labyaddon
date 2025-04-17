package cc.raynet.worldsharing.v1_21_5.mixins;

import cc.raynet.worldsharing.v1_21_5.client.PropertyStorage;
import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakePacketListenerImpl.class)
public class MixinServerHandshakePacketListenerImpl {

    @Unique
    private static final Gson worldsharing$gson = new Gson();
    @Shadow
    @Final
    private Connection connection;

    @Inject(method = "handleIntention", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerHandshakePacketListenerImpl;beginLogin(Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;Z)V",
            ordinal = 0))
    public void storeProperties(ClientIntentionPacket packet, CallbackInfo ci) {
        if (packet.intention().equals(ClientIntent.LOGIN) && packet.hostName().split("\00").length > 1) {
            ((PropertyStorage) connection).worldsharing$setProperties(worldsharing$gson.fromJson(packet.hostName()
                    .split("\00")[1], Property[].class));
        }
    }
}
