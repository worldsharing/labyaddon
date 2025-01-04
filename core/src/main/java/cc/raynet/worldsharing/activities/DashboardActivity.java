package cc.raynet.worldsharing.activities;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.model.Player;
import cc.raynet.worldsharing.protocol.packets.PacketVisibilityUpdate;
import cc.raynet.worldsharing.protocol.types.ConnectionState;
import cc.raynet.worldsharing.utils.VersionBridge;
import cc.raynet.worldsharing.utils.VersionStorage;
import cc.raynet.worldsharing.utils.model.GameDifficulty;
import cc.raynet.worldsharing.utils.model.GameMode;
import cc.raynet.worldsharing.utils.model.WorldVisibility;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.popup.SimpleAdvancedPopup;
import net.labymod.api.client.gui.screen.widget.widgets.popup.SimpleAdvancedPopup.SimplePopupButton;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.HrWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;


@AutoActivity
@Link("dashboard.lss")
public class DashboardActivity extends Activity {

    private final WorldsharingAddon addon;

    private int port;
    private FlexibleContentWidget options;

    public DashboardActivity(WorldsharingAddon addon) {
        this.addon = addon;

    }

    @Override
    public void initialize(Parent parent) {
        super.initialize(parent);
        if (!addon.isConnected() || VersionStorage.bridge == null) {
            return;
        }
        VersionBridge bridge = VersionStorage.bridge;

        FlexibleContentWidget container = new FlexibleContentWidget().addId("container");

        options = new FlexibleContentWidget();
        options.addId("options");

        if (port == 0) {
            port = bridge.getSuitableLanPort();
        }

        // Allow Cheats
        SwitchWidget allowCheatsSwitch = SwitchWidget.create(bridge::setCheatsEnabled);
        allowCheatsSwitch.setValue(bridge.cheatsEnabled());

        // Difficulty
        DropdownWidget<GameDifficulty> difficultyDropDown = new DropdownWidget<>();
        difficultyDropDown.addAll(GameDifficulty.values());
        difficultyDropDown.setSelected(bridge.getDifficulty());
        difficultyDropDown.setChangeListener(bridge::changeDifficulty);

        // GameMode
        DropdownWidget<GameMode> gameModeDropDown = new DropdownWidget<>();
        gameModeDropDown.addAll(GameMode.values());
        gameModeDropDown.setSelected(bridge.getGameMode());
        gameModeDropDown.setChangeListener(bridge::changeGameMode);

        // Max Slots
        SliderWidget maxSlotsSlider = new SliderWidget(e -> bridge.setSlots((int) e)).range(2, 16);
        maxSlotsSlider.setValue(bridge.getSlots());

        // Port Input
        TextFieldWidget portInput = new TextFieldWidget();
        portInput.placeholder(Component.translatable("worldsharing.menu.dashboard.port"));
        portInput.updateListener(e -> port = e.isEmpty() ? 0 : Integer.parseInt(e));
        portInput.maximalLength(5);
        portInput.validator(s -> {
            if (s == null || s.isEmpty()) {
                return true;
            }
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        portInput.setText(String.valueOf(port));
        portInput.setEditable(!bridge.isPublished());


        // Visibility
        DropdownWidget<WorldVisibility> visibilityDropDown = new DropdownWidget<>();
        visibilityDropDown.addAll(WorldVisibility.values());
        visibilityDropDown.setSelected(addon.sessionHandler.tunnelInfo.visibility);
        visibilityDropDown.setChangeListener(e -> addon.sessionHandler.sendPacket(new PacketVisibilityUpdate(e), b -> addon.sessionHandler.tunnelInfo.visibility = e));


        addOption("allow_cheats", allowCheatsSwitch);
        addOption("difficulty", difficultyDropDown);
        addOption("game_mode", gameModeDropDown);
        addOption("slots", maxSlotsSlider);
        addOption("port", portInput);
        addOption(Component.translatable("worldsharing.messages.visibility"), visibilityDropDown);

        container.addContent(switch (addon.sessionHandler.getState()) {
            case CONNECTED -> ComponentWidget.i18n("worldsharing.enums.status.connected");
            case CONNECTING -> ComponentWidget.i18n("worldsharing.enums.status.connecting");
            default -> {
                if (addon.sessionHandler.getState() == ConnectionState.DISCONNECTED && bridge.isPublished() && addon.sessionHandler.lastError == null) {
                    yield ComponentWidget.i18n("worldsharing.messages.shared_to_lan");
                }
                if (addon.sessionHandler.lastError == null) {
                    yield ComponentWidget.i18n("worldsharing.enums.status.disconnected");
                }
                yield ComponentWidget.component(Component.translatable("worldsharing.messages.connection_failed", NamedTextColor.RED, Component.text("\n\n" + addon.sessionHandler.lastError, NamedTextColor.WHITE)));
            }
        }).addId("info");

        FlexibleContentWidget sides = new FlexibleContentWidget();
        sides.addId("sides");
        sides.addContent(options);
        sides.addContent(new HrWidget());

        FlexibleContentWidget playerManagement = new FlexibleContentWidget();
        playerManagement.addId("player-management");
        playerManagement.addContent(ComponentWidget.component(addon.sessionHandler.players.isEmpty() ? Component.translatable("worldsharing.messages.empty_world") : Component.translatable("worldsharing.messages.world_player_count", Component.text(addon.sessionHandler.players.size())))
                .addId("text"));

        FlexibleContentWidget players = new FlexibleContentWidget();
        players.addId("players");


        for (Player p : addon.sessionHandler.players) {
            FlexibleContentWidget player = new FlexibleContentWidget();
            player.addId("player");

            IconWidget headWidget = new IconWidget(Icon.head(p.username));
            player.addContent(headWidget);

            ComponentWidget title = ComponentWidget.text(p.username + (p.isBedrock ? " (Bedrock)" : ""));
            player.addContent(title);

            FlexibleContentWidget buttons = new FlexibleContentWidget();
            buttons.addId("buttons");

            buttons.addContent(ButtonWidget.component(Component.translatable("worldsharing.menu.kick"), () -> {
                var in = new TextFieldWidget();
                in.maximalLength(255);
                in.setEditable(true);
                in.placeholder(Component.translatable("worldsharing.menu.reason"));
                in.setFocused(true);
                SimpleAdvancedPopup.builder()
                        .title(Component.translatable("worldsharing.messages.kick", Component.text(p.username)))
                        .widget(() -> in)
                        .addButton(SimplePopupButton.create(Component.text("kick"), e -> {
                            String reason = in.getText();
                            if (reason.isEmpty()) reason = "You were kicked from the World";
                            bridge.kickPlayer(p.username, reason);
                            reloadDashboard();
                        }))
                        .build()
                        .displayAsActivity();

            }));
            player.addContent(buttons);
            players.addContent(player);
        }

        playerManagement.addContent(players);

        sides.addContent(playerManagement);

        // Share & Close button
        FlexibleContentWidget btnContainer = new FlexibleContentWidget();
        btnContainer.addId("button-container");

        ButtonWidget shareButton = new ButtonWidget();
        shareButton.setEnabled(!addon.sessionHandler.isConnected());
        shareButton.text().set(Component.translatable("worldsharing.menu.share"));
        shareButton.setActionListener(this::init);

        ButtonWidget closeButton = new ButtonWidget();
        closeButton.setEnabled(bridge.isPublished() || addon.sessionHandler.isConnected());
        closeButton.text().set(Component.translatable("worldsharing.menu.close"));
        closeButton.setActionListener(() -> {
            if (!addon.sessionHandler.isConnected() && bridge.isPublished()) {
                bridge.stopServer();
                reload();
                return;
            }
            if (addon.sessionHandler.players.isEmpty()) {
                addon.sessionHandler.disconnect();
                return;
            }
            SimpleAdvancedPopup.builder()
                    .title(Component.translatable("worldsharing.messages.warn_stop_sharing"))
                    .description(Component.translatable("worldsharing.messages.warn_players_kicked", Component.text(addon.sessionHandler.players.size())))
                    .addButton(SimplePopupButton.create(Component.translatable("worldsharing.menu.close"), e -> addon.sessionHandler.disconnect()))
                    .build()
                    .displayAsActivity();
        });

        ButtonWidget openToLan = new ButtonWidget();
        openToLan.text().set(Component.translatable("menu.shareToLan"));
        openToLan.setEnabled(!bridge.isPublished());
        openToLan.setActionListener(() -> {
            bridge.publishLanWorld(port, bridge.getGameMode(), bridge.cheatsEnabled());
            reload();
        });

        btnContainer.addContent(shareButton);
        btnContainer.addContent(closeButton);
        btnContainer.addContent(openToLan);


        container.addContent(sides);
        container.addContent(btnContainer);
        document.addChild(container);
    }

    private void addOption(String value, Widget widget) {
        addOption(Component.translatable("worldsharing.menu.dashboard." + value), widget);
    }

    private void addOption(Component component, Widget widget) {
        FlexibleContentWidget container = new FlexibleContentWidget();
        container.addId("option");

        container.addContent(ComponentWidget.component(component).addId("label"));
        if (widget != null) {
            widget.addId("widget");
            container.addContent(widget);
        }
        options.addContent(container);
    }

    private synchronized void init() {
        VersionBridge bridge = VersionStorage.bridge;

        if (bridge.isPublished() || bridge.publishLanWorld(port, bridge.getGameMode(), bridge.cheatsEnabled())) {
            addon.sessionHandler.init();
        } else {
            WorldsharingAddon.LOGGER.warn("failed to publish lan world");
        }
    }

    public void reloadDashboard() {
        if (isOpen()) {
            if (Laby.labyAPI().minecraft().isOnRenderThread()) {
                this.reload();
            } else {
                Laby.labyAPI().minecraft().executeOnRenderThread(this::reload);
            }
        }
    }

}
