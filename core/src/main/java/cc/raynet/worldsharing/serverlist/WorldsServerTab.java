package cc.raynet.worldsharing.serverlist;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.ScreenInstance;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.multiplayer.ServerInfoWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.navigation.tab.Tab;
import net.labymod.api.client.network.server.ConnectableServerData;
import net.labymod.api.client.network.server.ServerInfoCache;
import net.labymod.core.client.gui.screen.activity.activities.multiplayer.child.ServerListActivity;
import net.labymod.core.client.gui.screen.widget.widgets.multiplayer.LanServerInfoWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


@AutoActivity
public class WorldsServerTab extends ServerListActivity<ConnectableServerData, ServerInfoWidget<ConnectableServerData>> {

    private final Map<ConnectableServerData, ServerInfoCache<ConnectableServerData>> worlds = new HashMap<>();

    public WorldsServerTab() {
        super("ws_worlds_tab",null);
    }

    private void hide(boolean hide) {
//        WorldsharingAddon.getWorldsAsTab().setHidden(hide);

//        if (hide) {
//            if (this.getActiveTab.get() == WorldsharingAddon.getWorldsAsTab()) {
//                Laby.labyAPI().minecraft().executeOnRenderThread(() -> switchTab.apply("private"));
//            }
//        }

//        this.reloadMultiplayer.run();

    }

    @Override
    protected void fillServerList(VerticalListWidget<ServerInfoWidget<ConnectableServerData>> verticalListWidget, String s) {
        for (ServerInfoCache<ConnectableServerData> serverCache : this.worlds.values()) {
            this.addServerWidget(serverCache, false);
        }
    }

    private void addServerWidget(ServerInfoCache<ConnectableServerData> cache, boolean initialize) {
        LanServerInfoWidget serverInfoWidget = new LanServerInfoWidget(cache);
        serverInfoWidget.setMovable(ServerInfoWidget.Movable.ADD, (movable) -> {
            serverInfoWidget.serverData().connect();
        });
        if (initialize) {
            this.serverListWidget.addChildInitialized(serverInfoWidget);
        } else {
            this.serverListWidget.addChild(serverInfoWidget);
        }

    }

    public void addWorld(ConnectableServerData serverData, Runnable runnable) {
        if (!this.worlds.containsKey(serverData)) {
            ServerInfoCache<ConnectableServerData> cache = new ServerInfoCache<>(serverData, null);
            this.worlds.put(serverData, cache);
            this.labyAPI.minecraft().executeOnRenderThread(() -> {
                if (this.document.isInitialized()) {
                    this.addServerWidget(cache, true);
                }
                if (runnable != null) runnable.run();
            });
            cache.setTimeout(2500);
            cache.update();
        }
        hide(this.worlds.isEmpty());
    }

    @Override
    protected void fillButtonContainer(FlexibleContentWidget container) {
        container.addFlexibleContent(this.joinButton);
    }

    public void removeWorld(String host) {
        for (var w : this.worlds.entrySet()) {
            if (w.getKey().address().getHost().equals(host)) {
                this.worlds.remove(w.getKey());
                Laby.labyAPI().minecraft().executeOnRenderThread(() -> {
                    for (Widget widget : serverListWidget.findChildrenIf((widgetX) -> widgetX.serverData()
                            .address()
                            .equals(w.getValue().serverAddress()))) {
                        if (widget instanceof ServerInfoWidget w1) {
                            serverListWidget.removeChild(w1);
                        }
                    }
                });
                break;
            }
        }
        hide(this.worlds.isEmpty());
    }

    public void clearWorlds() {
        this.worlds.clear();
    }

    @Override
    protected void refresh(boolean b) {
        for (ServerInfoCache<ConnectableServerData> cache : this.worlds.values()) {
            cache.update();
        }

        Laby.labyAPI().minecraft().executeOnRenderThread(this::reload);
    }
}