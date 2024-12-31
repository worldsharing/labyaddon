package cc.raynet.worldsharing.v1_12_2.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.Laby;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public abstract class MixinPauseScreen extends GuiScreen {

    @Inject(method = "initGui", at = @At("TAIL"))
    private void changeShareToLanButtonText(CallbackInfo ci) {
        if (!WorldsharingAddon.INSTANCE.isConnected()) {
            return;
        }

        if (Minecraft.getMinecraft().getIntegratedServer() == null) {
            return;
        }

        var openToLanButton = this.buttonList.get(3);
        if (openToLanButton == null) {
            return;
        }

        openToLanButton.displayString = I18n.getTranslation(openToLanButton.enabled ? "worldsharing.menu.invite_friends" : "worldsharing.menu.manage_world");
        openToLanButton.enabled = true;
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void modifyShareToLanInteraction(GuiButton guiButton, CallbackInfo ci) {
        if (guiButton.id == 7 && WorldsharingAddon.INSTANCE.isConnected()) {
            ci.cancel();
            Laby.labyAPI().minecraft().minecraftWindow().displayScreen(WorldsharingAddon.INSTANCE.dashboardActivity);
        }
    }
}