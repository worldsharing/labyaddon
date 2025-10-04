package cc.raynet.worldsharing.v1_21_8.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.Laby;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen {

    @Shadow
    @Final
    private static Component SHARE_TO_LAN;
    @Shadow
    @Final
    private static Component PLAYER_REPORTING;

    @Shadow
    protected abstract Button openScreenButton(Component component, Supplier<Screen> screenSupplier);

    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;"))
    private Button createPauseMenuButton(PauseScreen instance, Component component, Supplier<Screen> screenSupplier) {
        if (Minecraft.getInstance()
                .getSingleplayerServer() != null && (component.equals(SHARE_TO_LAN) || component.equals(PLAYER_REPORTING) && Minecraft.getInstance()
                .getSingleplayerServer()
                .isPublished()) && WorldsharingAddon.INSTANCE.hasAccess()) {

            return Button.builder(Component.literal(I18n.getTranslation(component.equals(SHARE_TO_LAN) ? "worldsharing.menu.invite_friends" : "worldsharing.menu.manage_world")), button -> Laby.labyAPI()
                    .minecraft()
                    .minecraftWindow()
                    .displayScreen(WorldsharingAddon.INSTANCE.dashboardActivity)).width(98).build();

        }
        return openScreenButton(component, screenSupplier);
    }
}
