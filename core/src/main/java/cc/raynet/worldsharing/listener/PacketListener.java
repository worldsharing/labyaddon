package cc.raynet.worldsharing.listener;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.serverlist.WorldsServerTab;
import cc.raynet.worldsharing.utils.AddonMessageUtil;
import net.labymod.api.Laby;
import net.labymod.api.client.network.server.ConnectableServerData;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.labymod.labyconnect.session.LabyConnectDisconnectEvent;
import net.labymod.api.event.labymod.labyconnect.session.friend.LabyConnectFriendStatusEvent;
import net.labymod.api.labyconnect.protocol.model.friend.Friend;
import net.labymod.core.event.labymod.PacketAddonDevelopmentEvent;

import java.util.UUID;

public class PacketListener {

    private final WorldsharingAddon addon;

    public PacketListener(WorldsharingAddon addon) {
        this.addon = addon;
    }

    @Subscribe
    public void onPacket(PacketAddonDevelopmentEvent event) {
        if (!event.packet().getKey().equals(addon.addonInfo().getNamespace()) || Laby.labyAPI()
                .labyConnect()
                .getSession() == null) {
            return;
        }

        Friend friend = Laby.labyAPI().labyConnect().getSession().getFriend(event.packet().getSender());
        if (friend == null) {
            return;
        }

        if (event.packet().getData().length < 1) {
            return;
        }

        byte action = event.packet().getData()[0];

        String host = friend.getName() + "." + WorldsharingAddon.WORLD_HOST_DOMAIN;

        switch (action) {
            case AddonMessageUtil.ACTION_ADD-> WorldsharingAddon.worldsServerScreen.addWorld(ConnectableServerData.builder()
                    .name(friend.getName() + "'s World")
                    .address(host)
                    .build(),null
            );
            case AddonMessageUtil.ACTION_REMOVE -> WorldsharingAddon.worldsServerScreen.removeWorld(host);
            case AddonMessageUtil.ACTION_INVITE -> addon.pushInviteNotification(host);
        }
    }

    @Subscribe
    public void sendToServerList(LabyConnectFriendStatusEvent event) {
        if (addon.sessionHandler.isConnected() && event.isOnline()) {
            AddonMessageUtil.send(new UUID[]{event.friend().getUniqueId()}, AddonMessageUtil.ACTION_ADD);
        }
    }

    @Subscribe
    public void onDisconnect(LabyConnectDisconnectEvent event) {
        WorldsharingAddon.worldsServerScreen.clearWorlds();
    }

}
