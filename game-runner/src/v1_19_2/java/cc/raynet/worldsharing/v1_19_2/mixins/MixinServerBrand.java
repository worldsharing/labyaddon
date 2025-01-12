package cc.raynet.worldsharing.v1_19_2.mixins;

import cc.raynet.worldsharing.utils.Utils;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerList.class)
public class MixinServerBrand {

    @ModifyArg(
            method = "placeNewPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeUtf(Ljava/lang/String;)Lnet/minecraft/network/FriendlyByteBuf;")
    )
    public String changeBrand(String brand) {
        return Utils.getBrand(brand);
    }
}
