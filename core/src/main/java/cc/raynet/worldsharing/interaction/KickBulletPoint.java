package cc.raynet.worldsharing.interaction;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.utils.VersionStorage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.entity.player.interaction.BulletPoint;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.popup.SimpleAdvancedPopup;

public class KickBulletPoint implements BulletPoint {

    @Override
    public Component getTitle() {
        return Component.translatable("worldsharing.menu.kick");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void execute(Player player) {
        if (WorldsharingAddon.INSTANCE.isConnected() & player != null) {
            var in = new TextFieldWidget();
            in.maximalLength(255);
            in.setEditable(true);
            in.placeholder(Component.translatable("worldsharing.menu.reason"));
            in.setFocused(true);
            SimpleAdvancedPopup.builder()
                    .title(Component.translatable("worldsharing.messages.kick", Component.text(player.profile().getUsername())))
                    .widget(() -> in)
                    .addButton(SimpleAdvancedPopup.SimplePopupButton.create(Component.translatable("worldsharing.menu.kick"), e -> {
                        String reason = in.getText();
                        if (reason.isEmpty()) reason = "You were kicked from the World";
                        VersionStorage.bridge.kickPlayer(player.profile().getUsername().toLowerCase(), reason);
                    }))
            .build()
            .displayAsActivity();

        }
    }

    @Override
    public boolean isVisible(Player playerInfo) {
        return BulletPoint.super.isVisible(playerInfo) && WorldsharingAddon.INSTANCE.isConnected();
    }
}
