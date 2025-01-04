package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.utils.model.WorldVisibility;
import net.labymod.api.Laby;
import net.labymod.api.labyconnect.protocol.model.friend.Friend;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddonMessageUtil {

    public static final byte ACTION_ADD = 1;
    public static final byte ACTION_REMOVE = 2;
    public static final byte ACTION_INVITE = 3;

    public static void send(UUID[] uuids, byte action) {
        if (action <= 0 || action > 3 || uuids == null || uuids.length < 1) {
            return;
        }

        if (Laby.labyAPI().labyConnect().getSession() == null) {
            return;
        }
        Laby.labyAPI()
                .labyConnect()
                .getSession()
                .sendAddonDevelopment(WorldsharingAddon.INSTANCE.addonInfo().getNamespace(), uuids, new byte[]{action});
    }

    public static String[] getFriendsUUIDs() {
        if (!Laby.labyAPI().labyConnect().isConnectionEstablished()) {
            return null;
        }
        List<Friend> labyFriends = Laby.labyAPI().labyConnect().getSession().getFriends();
        if (labyFriends == null) {
            return null;
        }

        List<String> friendsUUIDs = new ArrayList<>();
        for (Friend friend : labyFriends) {
            friendsUUIDs.add(friend.getUniqueId().toString().replace("-", ""));
        }
        return friendsUUIDs.toArray(String[]::new);
    }

    public static UUID[] getOnlineFriendsUUIDs() {
        if (Laby.labyAPI().labyConnect().getSession() == null) {
            return null;
        }
        List<Friend> labyFriends = Laby.labyAPI().labyConnect().getSession().getFriends();
        if (labyFriends == null) {
            return null;
        }
        List<UUID> friendsUUIDs = new ArrayList<>();
        SessionHandler sessionHandler = WorldsharingAddon.INSTANCE.sessionHandler;


        for (Friend friend : labyFriends) {
            if (!friend.isOnline()) {
                continue;
            }
            if (sessionHandler.isConnected() && sessionHandler.tunnelInfo.visibility == WorldVisibility.INVITE && !sessionHandler.whitelistedPlayers.contains(friend.getName())) {
                continue;
            }
            friendsUUIDs.add(friend.getUniqueId());
        }
        return friendsUUIDs.toArray(UUID[]::new);
    }

}
