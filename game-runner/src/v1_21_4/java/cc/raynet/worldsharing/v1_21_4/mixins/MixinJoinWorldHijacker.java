package cc.raynet.worldsharing.v1_21_4.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WorldSelectionList.WorldListEntry.class)
public class MixinJoinWorldHijacker {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    public void cancel(CallbackInfo ci) {
        //        ci.cancel();
        //        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.literal("connecting to hypixel")));
        //        Laby.references().serverController().joinServer("hypixel.net");
    }

}
