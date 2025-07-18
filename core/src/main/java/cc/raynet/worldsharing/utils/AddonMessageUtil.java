package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.utils.model.WorldVisibility;
import net.labymod.api.Laby;
import net.labymod.api.labyconnect.LabyConnectSession;
import net.labymod.api.labyconnect.protocol.model.friend.Friend;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AddonMessageUtil {

    public enum Action {
        ADD, REMOVE, INVITE;

        public static Action fromByte(byte b) {
            if (b < 0 || b >= Action.values().length) return null;
            return Action.values()[b];
        }
    }

    public static void send(UUID[] uuids, Action action) {
        if (uuids == null || uuids.length < 1) {
            return;
        }

        if (Laby.labyAPI().labyConnect().getSession() == null) {
            return;
        }
        Laby.labyAPI()
                .labyConnect()
                .getSession()
                .sendAddonDevelopment(WorldsharingAddon.INSTANCE.addonInfo().getNamespace(), uuids, new byte[]{(byte) action.ordinal()});
    }

    public static String[] getFriends() {
        if (!Laby.labyAPI().labyConnect().isConnectionEstablished()) {
            return null;
        }
        LabyConnectSession session = Laby.labyAPI().labyConnect().getSession();
        if (session == null) {
            return null;
        }

        List<String> friends = new ArrayList<>();
        for (Friend friend : session.getFriends()) {
            friends.add(friend.getName());
        }
        return friends.toArray(String[]::new);
    }

    public static UUID[] getOnlineFriendsUUIDs(SessionHandler sessionHandler) {
        if (Laby.labyAPI().labyConnect().getSession() == null) {
            return null;
        }
        List<Friend> labyFriends = Laby.labyAPI().labyConnect().getSession().getFriends();
        if (labyFriends == null) {
            return null;
        }
        List<UUID> friendsUUIDs = new ArrayList<>();

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