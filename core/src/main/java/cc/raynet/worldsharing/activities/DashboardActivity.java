package cc.raynet.worldsharing.activities;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.model.Player;
import cc.raynet.worldsharing.protocol.packets.PacketSlotUpdate;
import cc.raynet.worldsharing.protocol.packets.PacketVisibilityUpdate;
import cc.raynet.worldsharing.protocol.types.ConnectionState;
import cc.raynet.worldsharing.utils.WorldManager;
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
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.HorizontalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
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
    private int tempSlots;
    private Player hostPlayer;

    public DashboardActivity(WorldsharingAddon addon) {
        this.addon = addon;
    }

    @Override
    public void initialize(Parent parent) {
        super.initialize(parent);
        if (!addon.isConnected() || addon.manager() == null) {
            return;
        }
        WorldManager manager = addon.manager();

        FlexibleContentWidget container = new FlexibleContentWidget().addId("container");

        options = new FlexibleContentWidget();
        options.addId("options");

        if (port == 0) {
            port = manager.getSuitableLanPort();
        }

        if (tempSlots == 0) {
            tempSlots = manager.getSlots();
        }

        // Allow Cheats
        SwitchWidget allowCheatsSwitch = SwitchWidget.create(manager::setCheatsEnabled);
        allowCheatsSwitch.setValue(manager.cheatsEnabled());

        // Difficulty
        DropdownWidget<GameDifficulty> difficultyDropDown = new DropdownWidget<>();
        difficultyDropDown.addAll(GameDifficulty.values());
        difficultyDropDown.setSelected(manager.getDifficulty());
        difficultyDropDown.setChangeListener(manager::changeDifficulty);

        // GameMode
        DropdownWidget<GameMode> gameModeDropDown = new DropdownWidget<>();
        gameModeDropDown.addAll(GameMode.values());
        gameModeDropDown.setSelected(manager.getGameMode());
        gameModeDropDown.setChangeListener(manager::changeGameMode);

        // Max Slots

        SliderWidget maxSlotsSlider = new SliderWidget(val -> {
            if (addon.sessionHandler.isConnected()) {
                tempSlots = (int) val;
            } else {
                manager.setSlots((int) val);
            }
        }).range(2, (Laby.labyAPI().labyConnect().getSession() == null ? 12 : (Laby.labyAPI().labyConnect().getSession().self().gameUser().visibleGroup().isDefault() ? 12 : 32)));
        maxSlotsSlider.setValue(addon.sessionHandler.isConnected() ? tempSlots : manager.getSlots());


        // Max Slots Update Button
        ButtonWidget maxSlotsSaveButton = new ButtonWidget();
        maxSlotsSaveButton.text().set(Component.text("Update"));
        maxSlotsSaveButton.setActionListener(() -> {
            if (tempSlots != manager.getSlots()) {
                addon.sessionHandler.sendPacket(new PacketSlotUpdate((int) maxSlotsSlider.getValue()), f -> manager.setSlots((int) maxSlotsSlider.getValue()));
            }
        });

        FlexibleContentWidget maxSlotsContainer = new FlexibleContentWidget();
        maxSlotsContainer.addContent(maxSlotsSlider);
        maxSlotsContainer.addContent(maxSlotsSaveButton);
        maxSlotsContainer.addId("slotscontainer");

        // Port Input
        TextFieldWidget portInput = new TextFieldWidget().addId("port-input");
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
        portInput.setEditable(!manager.isPublished());

        // Visibility
        DropdownWidget<WorldVisibility> visibilityDropDown = new DropdownWidget<>();
        visibilityDropDown.addAll(WorldVisibility.values());
        visibilityDropDown.setSelected(addon.sessionHandler.tunnelInfo.visibility);
        visibilityDropDown.setChangeListener(e -> addon.sessionHandler.sendPacket(new PacketVisibilityUpdate(e), b -> addon.sessionHandler.tunnelInfo.visibility = e));

        addOption("allow_cheats", allowCheatsSwitch);
        addOption("difficulty", difficultyDropDown);
        addOption("game_mode", gameModeDropDown);
        addOption("slots", addon.sessionHandler.isConnected() ? maxSlotsContainer : maxSlotsSlider);
        addOption("port", portInput);
        addOption(Component.translatable("worldsharing.messages.visibility"), visibilityDropDown);

        container.addContent(switch (addon.sessionHandler.getState()) {
            case CONNECTED -> ComponentWidget.i18n("worldsharing.enums.status.connected");
            case CONNECTING -> ComponentWidget.i18n("worldsharing.enums.status.connecting");
            default -> {
                if (addon.sessionHandler.getState() == ConnectionState.DISCONNECTED && manager.isPublished() && addon.sessionHandler.lastError == null) {
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
        playerManagement.addContent(ComponentWidget.component(addon.sessionHandler.players.isEmpty() ? Component.translatable("worldsharing.messages.empty_world") : Component.translatable("worldsharing.messages.world_player_count", Component.text(addon.sessionHandler.players.size()+1)))
                .addId("text"));

        VerticalListWidget<Widget> playerList = new VerticalListWidget<>().addId("players");

        if (addon.sessionHandler.isConnected() || !addon.sessionHandler.players.isEmpty()) {
            playerList.addChild(addPlayerOptions(hostPlayer));
            for (Player p : addon.sessionHandler.players) {
                playerList.addChild(addPlayerOptions(p));
            }
        }

        playerManagement.addContent(new ScrollWidget(playerList).addId("players-scroll"));

        sides.addContent(playerManagement);

        // Share & Close button
        FlexibleContentWidget btnContainer = new FlexibleContentWidget();
        btnContainer.addId("button-container");

        ButtonWidget shareButton = new ButtonWidget();
        shareButton.setEnabled(!addon.sessionHandler.isConnected());
        shareButton.text().set(Component.translatable("worldsharing.menu.share"));
        shareButton.setActionListener(this::init);

        ButtonWidget closeButton = new ButtonWidget();
        closeButton.setEnabled(manager.isPublished() || addon.sessionHandler.isConnected());
        closeButton.text().set(Component.translatable("worldsharing.menu.close"));
        closeButton.setActionListener(() -> {
            if (!addon.sessionHandler.isConnected() && manager.isPublished()) {
                manager.stopServer();
                reload();
                return;
            }
            if (addon.sessionHandler.players.isEmpty()) {
                addon.sessionHandler.disconnect();
                reload();
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
        openToLan.setEnabled(!manager.isPublished());
        openToLan.setActionListener(() -> {
            if (!manager.publishLanWorld(port, manager.getGameMode(), manager.cheatsEnabled())) {
                port = manager.getSuitableLanPort();
            }
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
        HorizontalListWidget container = new HorizontalListWidget();
        container.addId("option");

        container.addEntry(ComponentWidget.component(component).addId("label"));
        if (widget != null) {
            widget.addId("widget");
            container.addEntry(widget);
        }
        options.addContent(container);
    }

    private synchronized void init() {
        WorldManager manager = addon.manager();
        hostPlayer = new Player(Laby.labyAPI().getName(), null, false, null);
        hostPlayer.gameMode = manager.getPlayerGameMode(hostPlayer.username);

        if (manager.isPublished() || manager.publishLanWorld(port, manager.getGameMode(), manager.cheatsEnabled())) {
            tempSlots = manager.getSlots();
            addon.sessionHandler.init();
        } else {
            WorldsharingAddon.LOGGER.warn("failed to publish lan world");
            port = manager.getSuitableLanPort();
        }
    }

    private FlexibleContentWidget addPlayerOptions(Player p) {
        FlexibleContentWidget player = new FlexibleContentWidget();
        player.addId("player");

        IconWidget headWidget = new IconWidget(Icon.head(p.username));
        player.addContent(headWidget);

        ComponentWidget title = ComponentWidget.text(p.username + (p.isBedrock ? " (Bedrock)" : ""));
        player.addContent(title);

        FlexibleContentWidget buttons = new FlexibleContentWidget();
        buttons.addId("buttons");

        DropdownWidget<GameMode> playerGameMode = new DropdownWidget<>();
        playerGameMode.addAll(GameMode.values());
        playerGameMode.setSelected(p.gameMode);
        playerGameMode.setChangeListener(gameMode -> {
            p.gameMode = gameMode;
            addon.manager().setPlayerGameMode(p.username, gameMode);
        });

        buttons.addContent(playerGameMode);

        buttons.addContent(ButtonWidget.component(Component.text(p.operator ? "Deop" : "Op"), () -> {
            addon.manager().setOperator(p.username, !p.operator);
            p.operator = !p.operator;
            reloadDashboard();
        }));

        if (!Laby.labyAPI().getName().equals(p.username)) {
            buttons.addContent(ButtonWidget.component(Component.translatable("worldsharing.menu.kick"), () -> {
                var in = new TextFieldWidget();
                in.maximalLength(255);
                in.setEditable(true);
                in.placeholder(Component.translatable("worldsharing.menu.reason"));
                in.setFocused(true);
                SimpleAdvancedPopup.builder()
                    .title(Component.translatable("worldsharing.messages.kick", Component.text(p.username)))
                    .widget(() -> in)
                    .addButton(SimplePopupButton.create(Component.translatable("worldsharing.menu.kick"), e -> {
                        String reason = in.getText();
                        if (reason.isEmpty()) reason = "You were kicked from the World";
                        addon.manager().kickPlayer(p.username, reason);
                        reloadDashboard();
                    }))
                    .build()
                    .displayAsActivity();
            }));

        }

        player.addContent(buttons);
        return player;
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
