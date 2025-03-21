package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.Laby;

import java.net.SocketAddress;
import java.security.SecureRandom;
import java.util.Base64;

import static cc.raynet.worldsharing.WorldsharingAddon.WORLD_HOST_PATTERN;

public class Utils {

    private static final SecureRandom random = new SecureRandom();
    public static SocketAddress proxyChannelAddress;

    public static boolean isLanWorldDomain(String s) {
        return WORLD_HOST_PATTERN.matcher(s).matches();
    }

    public static String randomString(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, length);
    }

    public static String getBrand(String fallback) {
        return WorldsharingAddon.INSTANCE.sessionHandler.isConnected() ? Laby.labyAPI().getName() + "'s world" : fallback;
    }

}
