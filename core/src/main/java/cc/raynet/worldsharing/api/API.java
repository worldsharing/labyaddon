package cc.raynet.worldsharing.api;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.utils.CryptUtils;
import cc.raynet.worldsharing.utils.Utils;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.util.Pair;
import net.labymod.api.util.io.web.exception.WebRequestException;
import net.labymod.api.util.io.web.request.Request;
import net.labymod.api.util.io.web.request.Request.Method;
import net.labymod.api.util.io.web.request.Response;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class API {

    private final static String endpoint = "https://" + WorldsharingAddon.GATEWAY_DOMAIN;
    @Nullable public static Pair<String, InetAddress> selectedNode; // name -> ip
    private static Node closestNode;
    @Nullable public static CryptUtils.ECCKeyPair keyPair = null;

    public static InetAddress getClosestNode(InetAddress address) {
        if (!WorldsharingAddon.INSTANCE.configuration().enabled().get() || !Utils.isLanWorldDomain(address.getHostName())) {
            return address;
        }
        return selectedNode != null ? selectedNode.getSecond() : safeGetByName(closestNode.host, address);
    }

    public static void init() {
        try {
            keyPair = CryptUtils.generateECCKeyPair();
            WorldsharingAddon.INSTANCE.nodes = getNodes();
            closestNode = getClosestNode();
        } catch (Exception e) {
            WorldsharingAddon.LOGGER.debug("API Handler init failed", e.getCause());
        }
    }

    private static InetAddress safeGetByName(String address, InetAddress fallback) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            return fallback;
        }
    }

    public static Control getControl() throws WebRequestException {
        Response<Control> req = Request.ofGson(new TypeToken<Control>() {})
                .method(Method.GET)
                .url(endpoint+"/connect/control")
                .handleErrorStream()
                .executeSync();
        if (req.hasException()) {
            throw req.exception();
        }

        if (req.getStatusCode() != 200) {
            throw new WebRequestException(new Exception("Unexpected response code:" + req.getStatusCode()));
        }

        return req.get();
    }

    private static Node getClosestNode() throws WebRequestException {
        Response<Node> req = Request.ofGson(new TypeToken<Node>() {})
            .method(Method.GET)
            .url(endpoint+"/relay")
            .handleErrorStream()
            .executeSync();
        if (req.hasException()) {
            throw req.exception();
        }

        if (req.getStatusCode() != 200) {
            throw new WebRequestException(new Exception("Unexpected response code: " + req.getStatusCode()));
        }

        return req.get();
    }

    public static Map<String, InetAddress> getNodes() throws WebRequestException {
        Response<Map<String, InetAddress>> req = Request.ofGson(new TypeToken<Map<String, InetAddress>> (){})
                .method(Method.GET)
                .url(endpoint + "/connect/nodes")
                .handleErrorStream()
                .executeSync();

        if (req.hasException()) {
            throw req.exception();
        }

        if (req.getStatusCode() != 200) {
            throw new WebRequestException(new Exception("Unexpected response code: " + req.getStatusCode()));
        }

        return req.get();
    }

    private static class Node {
        String name;
        String host;
    }

    public static class Control {
        public String host;
        public int port;
        public String key;
    }
}
