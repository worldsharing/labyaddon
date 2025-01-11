package cc.raynet.worldsharing.v1_21_4.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientIntentionPacket.class)
public class MixinClientIntentionPacket {

     @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", constant = @Constant(intValue = 255))
     private static int increaseMaxValue(int i) {
         if (WorldsharingAddon.INSTANCE.isConnected()) {
             return Short.MAX_VALUE;
         }
         return i;
     }
}
