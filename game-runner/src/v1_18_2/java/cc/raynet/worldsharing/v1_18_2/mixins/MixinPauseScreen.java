package cc.raynet.worldsharing.v1_18_2.mixins;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.Laby;
import net.labymod.api.util.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {

    protected MixinPauseScreen(Component lvt_1_1_) {
        super(lvt_1_1_);
    }

    @Shadow
    protected abstract void init();

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    private void onCreatePauseMenu(CallbackInfo ci) {
        if (!WorldsharingAddon.INSTANCE.hasAccess()) {
            return;
        }

        if (Minecraft.getInstance().getSingleplayerServer() == null) {
            return;
        }

        children().remove(6);

        addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, Component.nullToEmpty(I18n.getTranslation(Minecraft.getInstance()
                .getSingleplayerServer()
                .isPublished() ? "worldsharing.menu.manage_world" : "worldsharing.menu.invite_friends")), b -> Laby.labyAPI()
                .minecraft()
                .minecraftWindow()
                .displayScreen(WorldsharingAddon.INSTANCE.dashboardActivity)));
    }
}