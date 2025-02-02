package cc.raynet.worldsharing.v1_21.mixins;

import cc.raynet.worldsharing.utils.Utils;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class MixinServerBrand {

    @ModifyArg(
            method = "startConfiguration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/custom/BrandPayload;<init>(Ljava/lang/String;)V")
    )
    public String changeBrand(String brand) {
        return Utils.getBrand(brand);
    }
}
