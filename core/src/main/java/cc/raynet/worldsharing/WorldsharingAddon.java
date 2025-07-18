package cc.raynet.worldsharing;

import cc.raynet.worldsharing.activities.DashboardActivity;
import cc.raynet.worldsharing.command.DebugCommand;
import cc.raynet.worldsharing.command.WhitelistCommand;
import cc.raynet.worldsharing.config.AddonConfiguration;
import cc.raynet.worldsharing.core.generated.DefaultReferenceStorage;
import cc.raynet.worldsharing.interaction.KickBulletPoint;
import cc.raynet.worldsharing.navigation.NavigationElement;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.protocol.tunnel.AbstractTunnel;
import cc.raynet.worldsharing.utils.WorldManager;
import io.sentry.Sentry;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.util.logging.Logging;

@AddonMain
public class WorldsharingAddon extends LabyAddon<AddonConfiguration> {

    public static Logging LOGGER;
    public static WorldsharingAddon INSTANCE;
    private WorldManager worldManager;

    public SessionHandler sessionHandler;
    public DashboardActivity dashboardActivity;

    @Override
    protected void enable() {
        this.registerSettingCategory();
        worldManager = ((DefaultReferenceStorage) this.referenceStorageAccessor()).worldManager();
        INSTANCE = this;
        LOGGER = logger();

        if (!labyAPI().labyModLoader().isAddonDevelopmentEnvironment()) {
            Sentry.init((options) -> {
                options.setDsn("https://d65276bb7799e005d073f28106e8db69@sentry.rappytv.com/3");
                options.setTracesSampleRate(1.0);
            });
        }

        sessionHandler = new SessionHandler(this);
        dashboardActivity = new DashboardActivity(this);

        if (Laby.labyAPI().minecraft().getProtocolVersion() < 758) { // 1.18.2
            AbstractTunnel.preferredType = AbstractTunnel.Type.KWIK;
        }

        labyAPI().navigationService().register("ws_nav", new NavigationElement("ws_nav", dashboardActivity));
        registerCommand(new WhitelistCommand(this));
        registerCommand(new DebugCommand(this));

        configuration().enabled().addChangeListener(v -> {
            if (!v && sessionHandler.isConnected()) {
                sessionHandler.disconnect();
            }
        });

        this.labyAPI().interactionMenuRegistry().register(new KickBulletPoint(this));
    }

    public boolean hasAccess() {
        return configuration().enabled().get() && labyAPI().minecraft()
                .sessionAccessor()
                .isPremium() && labyAPI().minecraft().isSingleplayer();
    }

    public WorldManager manager() {
        return worldManager;
    }

    @Override
    protected Class<? extends AddonConfiguration> configurationClass() {
        return AddonConfiguration.class;
    }
}
