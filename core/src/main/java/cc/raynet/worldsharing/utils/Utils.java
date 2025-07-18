package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.WorldsharingAddon;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.labymod.api.Laby;
import net.labymod.api.labyconnect.LabyConnectSession;
import net.labymod.api.labyconnect.TokenStorage;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class Utils {

    // Credits: https://github.com/Gaming32/world-host/
    public static Constructor<? extends ChannelInitializer<Channel>> channelInitConstructor = null;
    public static SocketAddress proxyChannelAddress;

    public static String getBrand(String fallback) {
        if (WorldsharingAddon.INSTANCE == null || WorldsharingAddon.INSTANCE.sessionHandler == null || !WorldsharingAddon.INSTANCE.sessionHandler.isConnected()) {
            return fallback;
        }
        return Laby.labyAPI().getName() + "'s world";
    }


    public static @Nullable String getLabyConnectToken() {
        LabyConnectSession session = Laby.labyAPI().labyConnect().getSession();
        if(session == null) return null;

        TokenStorage.Token token = session.tokenStorage().getToken(
                TokenStorage.Purpose.JWT,
                session.self().getUniqueId()
        );

        if(token == null || token.isExpired()) return null;

        return token.getToken();
    }

    public static InetSocketAddress splitHostAndPort(String address) throws IllegalArgumentException {
        String host;
        int port;

        if (address.startsWith("[")) {
            int bracketEnd = address.indexOf(']');
            if (bracketEnd == -1 || address.length() <= bracketEnd + 2 || address.charAt(bracketEnd + 1) != ':') {
                throw new IllegalArgumentException("Invalid IPv6 address format");
            }
            host = address.substring(1, bracketEnd);
            port = Integer.parseInt(address.substring(bracketEnd + 2));
        } else {
            int colonIndex = address.lastIndexOf(':');
            if (colonIndex == -1) {
                throw new IllegalArgumentException("Missing port");
            }
            host = address.substring(0, colonIndex);
            port = Integer.parseInt(address.substring(colonIndex + 1));
        }

        return new InetSocketAddress(host, port);
    }
}
