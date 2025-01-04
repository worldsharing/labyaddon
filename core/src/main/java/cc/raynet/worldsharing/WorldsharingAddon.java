package cc.raynet.worldsharing;

import cc.raynet.worldsharing.activities.DashboardActivity;
import cc.raynet.worldsharing.api.APIHandler;
import cc.raynet.worldsharing.command.DebugCommand;
import cc.raynet.worldsharing.command.WhitelistCommand;
import cc.raynet.worldsharing.config.AddonConfiguration;
import cc.raynet.worldsharing.navigation.NavigationElement;
import cc.raynet.worldsharing.protocol.SessionHandler;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.util.logging.Logging;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

@AddonMain
public class WorldsharingAddon extends LabyAddon<AddonConfiguration> {

    public static final String GATEWAY_DOMAIN = "worldshar.ing";
    public static final String WORLD_HOST_DOMAIN = "lan.laby.net";
    public static final Pattern WORLD_HOST_PATTERN = Pattern.compile("^[^.]+\\." + Pattern.quote(WORLD_HOST_DOMAIN) + "$");

    public static Logging LOGGER;
    public static WorldsharingAddon INSTANCE;

    public SessionHandler sessionHandler;
    public DashboardActivity dashboardActivity;
    public APIHandler api;

    public List<String> bedrockPlayers = new CopyOnWriteArrayList<>();
    public Map<String, InetAddress> nodes = new HashMap<>();

    @Override
    protected void enable() {
        this.registerSettingCategory();
        INSTANCE = this;
        LOGGER = logger();
        //        Sentry.init((options) -> {
        //            options.setDsn("https://d65276bb7799e005d073f28106e8db69@sentry.rappytv.com/3");
        //            options.setTracesSampleRate(1.0);
        //        });

        api = new APIHandler(this);
        sessionHandler = new SessionHandler(this);
        dashboardActivity = new DashboardActivity(this);

        labyAPI().navigationService().register("ws_nav", new NavigationElement("ws_nav", dashboardActivity));
        registerCommand(new WhitelistCommand(this));
        registerCommand(new DebugCommand());
        Thread.ofVirtual().start(api::init);

        configuration().enabled().addChangeListener(v -> {
            if (!v && sessionHandler.isConnected()) {
                sessionHandler.disconnect();
            }
        });
    }

    public boolean isConnected() {
        return configuration().enabled().get() && labyAPI().minecraft()
                .sessionAccessor()
                .isPremium() && labyAPI().minecraft().isSingleplayer();
    }

    @Override
    protected Class<? extends AddonConfiguration> configurationClass() {
        return AddonConfiguration.class;
    }
}
