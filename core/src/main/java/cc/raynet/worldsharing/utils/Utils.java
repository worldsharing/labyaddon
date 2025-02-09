package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.Laby;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Hashtable;

import static cc.raynet.worldsharing.WorldsharingAddon.WORLD_HOST_PATTERN;

public class Utils {

    private static final SecureRandom random = new SecureRandom();

    public static boolean isLanWorldDomain(String s) {
        return WORLD_HOST_PATTERN.matcher(s).matches();
    }

    public static String randomString(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, length);
    }

    @Deprecated
    public static InetSocketAddress getTunnelControlAddr(final String supplier) throws NamingException {
        DirContext dircontext;
        try {
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> hashtable = new Hashtable<>();
            hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            hashtable.put("java.naming.provider.url", "dns://1.1.1.1");
            hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
            dircontext = new InitialDirContext(hashtable);
        } catch (Throwable throwable) {
            return new InetSocketAddress("connect." + WorldsharingAddon.GATEWAY_DOMAIN, 40886);
        }

        Attribute attribute = dircontext.getAttributes("_rayconnect._tcp." + supplier, new String[]{"SRV"}).get("srv");
        if (attribute != null) {
            String[] astring = attribute.get().toString().split(" ", 4);
            if (astring.length < 4) {
                throw new NamingException("Invalid SRV Response");
            }
            return new InetSocketAddress(astring[3], Integer.parseInt(astring[2]));
        }
        throw new NamingException("Could not find TCP SRV address");
    }

    public static String getBrand(String fallback) {
        return WorldsharingAddon.INSTANCE.sessionHandler.isConnected() ? Laby.labyAPI().getName() + "'s world" : fallback;
    }

}
