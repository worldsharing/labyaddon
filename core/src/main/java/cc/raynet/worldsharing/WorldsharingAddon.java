package cc.raynet.worldsharing;

import cc.raynet.worldsharing.activities.DashboardActivity;
import cc.raynet.worldsharing.api.APIHandler;
import cc.raynet.worldsharing.command.DebugCommand;
import cc.raynet.worldsharing.command.WhitelistCommand;
import cc.raynet.worldsharing.config.AddonConfiguration;
import cc.raynet.worldsharing.listener.PacketListener;
import cc.raynet.worldsharing.navigation.NavigationElement;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.serverlist.WorldsServerTab;
import io.sentry.Sentry;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.notification.Notification;
import net.labymod.api.notification.Notification.NotificationButton;
import net.labymod.api.notification.Notification.Type;
import net.labymod.api.util.logging.Logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

@AddonMain
public class WorldsharingAddon extends LabyAddon<AddonConfiguration> {

    // * constants *
    public static final String GATEWAY_DOMAIN = "worldshar.ing";
    public static final String WORLD_HOST_DOMAIN = "lan.laby.net";
    public static final Pattern WORLD_HOST_PATTERN = Pattern.compile("^[^.]+\\." + Pattern.quote(WORLD_HOST_DOMAIN) + "$");

    // * static *
    public static Logging LOGGER;
    public static WorldsharingAddon INSTANCE;

    public SessionHandler sessionHandler;
    public DashboardActivity dashboardActivity;
    public APIHandler api;

    public List<String> bedrockPlayers = new CopyOnWriteArrayList<>();
    public Map<String, InetAddress> nodes = new HashMap<>();

    public static WorldsServerTab worldsServerScreen;

    @Override
    protected void enable() {
        this.registerSettingCategory();
        INSTANCE = this;
        LOGGER = logger();
        if (!configuration().enabled().get()) {
            return;
        }
//        Sentry.init((options) -> {
//            options.setDsn("https://d65276bb7799e005d073f28106e8db69@sentry.rappytv.com/3");
//            options.setTracesSampleRate(1.0);
//        });

        api = new APIHandler(this);
        sessionHandler = new SessionHandler(this);
        dashboardActivity = new DashboardActivity(this);

        worldsServerScreen = new WorldsServerTab();

        labyAPI().navigationService().register("ws_nav_element", new NavigationElement(Component.text("ws_nav"), "worldsharing_nav_element",false, dashboardActivity));
        labyAPI().navigationService().register("ws_worlds_tab", new NavigationElement(Component.text("Worlds"), "worldsharing_worlds_tab", true, worldsServerScreen));

        registerCommand(new WhitelistCommand(this));
        registerCommand(new DebugCommand());
        registerListener(new PacketListener(this));

        Thread.ofVirtual().start(api::init);
    }

    public boolean isConnected() {
        return configuration().enabled().get()
                && labyAPI().minecraft().sessionAccessor().isPremium()
                && labyAPI().minecraft().isSingleplayer();
    }

    public void pushInviteNotification(String host) { // temporary, no need for i18n
        Notification.builder()
                .title(Component.text("World Sharing"))
                .text(Component.text(String.format("Join %s's world", host.substring(0, host.length() - WORLD_HOST_DOMAIN.length() - 1))))
                .type(Type.SOCIAL)
                .addButton(NotificationButton.primary(Component.text("Join"), () -> labyAPI().serverController()
                        .joinServer(host)))
                .buildAndPush();
    }

    @Override
    protected Class<? extends AddonConfiguration> configurationClass() {
        return AddonConfiguration.class;
    }
}
