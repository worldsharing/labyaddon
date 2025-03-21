package cc.raynet.worldsharing.v1_8_9.mixins;

import cc.raynet.worldsharing.utils.Utils;
import net.minecraft.server.management.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerConfigurationManager.class)
public class MixinServerBrand {

    @ModifyArg(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/PacketBuffer;writeString(Ljava/lang/String;)Lnet/minecraft/network/PacketBuffer;"))
    public String changeBrand(String brand) {
        return Utils.getBrand(brand);
    }
}
