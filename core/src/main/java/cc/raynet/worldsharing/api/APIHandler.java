package cc.raynet.worldsharing.api;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.utils.CryptUtils;
import cc.raynet.worldsharing.utils.Utils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.util.Pair;
import net.labymod.api.util.io.web.exception.WebRequestException;
import net.labymod.api.util.io.web.request.Request;
import net.labymod.api.util.io.web.request.Request.Method;
import net.labymod.api.util.io.web.request.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class APIHandler {

    private final WorldsharingAddon addon;
    private final String endpoint = "https://" + WorldsharingAddon.GATEWAY_DOMAIN + "/connect/";
    private final Gson gson;
    public Pair<String, InetAddress> selectedNode; // name -> ip
    public Map<String, Integer> pings = new ConcurrentHashMap<>();

    public APIHandler(WorldsharingAddon addon) {
        this.addon = addon;
        gson = new GsonBuilder().registerTypeAdapter(InetAddress.class, new InetAddressDeserializer()).create();
    }

    public static InetAddress getClosestNode(InetAddress address) {
        if (!Utils.isLanWorldDomain(address.getHostName())) {
            return address;
        }
        return WorldsharingAddon.INSTANCE.api.getClosestNode2(address);
    }

    public Map<String, Integer> getPings() {
        if (pings.isEmpty()) {
            try {
                pings = calculatePings();
            } catch (UnknownHostException e) {
                WorldsharingAddon.LOGGER.warn("Could not get pings from API: ", e.getMessage());
            }
        }
        return pings;
    }

    public void init() {
        try {
            pings = calculatePings();
            addon.nodes = getNodes();
        } catch (Exception e) {
            WorldsharingAddon.LOGGER.debug("API Handler init failed", e.getMessage());
        }
    }

    public InetAddress getClosestNode2(final InetAddress fallback) {
        if (selectedNode != null) {
            return selectedNode.getSecond();
        }
        try {
            CompletableFuture<Map<String, Integer>> hostPingDataFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getHostPingData(fallback.getHostName());
                } catch (WebRequestException e) {
                    WorldsharingAddon.LOGGER.debug("Failed to get host ping data: ", e);
                    return Collections.emptyMap();
                }
            });

            Map<String, Integer> pingData = addon.api.getPings();
            Map<String, Integer> hostPingData = hostPingDataFuture.get();

            hostPingData.forEach((ip, ping) -> pingData.merge(ip, ping, Integer::sum));

            String result = null;
            int minValue = Integer.MAX_VALUE;

            for (Map.Entry<String, Integer> entry : pingData.entrySet()) {
                if (entry.getValue() < minValue) {
                    minValue = entry.getValue();
                    result = entry.getKey();
                }
            }
            return result == null ? fallback : safeGetByName(result, fallback);

        } catch (Exception e) {
            WorldsharingAddon.LOGGER.debug("Error calculating closest node", e);
            return fallback;
        }
    }

    private InetAddress safeGetByName(String address, InetAddress fallback) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            return fallback;
        }
    }

    public Map<String, Integer> calculatePings() throws UnknownHostException {
        Map<String, Integer> result = new HashMap<>();
        InetAddress[] ips = InetAddress.getAllByName("relays." + WorldsharingAddon.GATEWAY_DOMAIN);
        for (InetAddress ip : ips) {
            try {
                result.put(ip.getHostAddress(), (int) TCPing(new InetSocketAddress(ip, 25565)));
            } catch (IOException e) {
                WorldsharingAddon.LOGGER.warn("failed to calculate pingdata " + e.getMessage());
            }
        }

        return result;
    }

    public Map<String, Integer> getHostPingData(String host) throws WebRequestException {
        Response<Map<String, Integer>> req = Request.ofGson(new TypeToken<Map<String, Integer>>() {
        }).method(Method.GET).url(endpoint + "pingData?host=" + host).handleErrorStream().executeSync();

        if (req.hasException()) {
            throw req.exception();
        }

        if (req.getStatusCode() != 200) {
            throw new WebRequestException(new Exception("Unexpected response code: " + req.getStatusCode()));
        }

        return req.get();
    }

    public PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Response<String> response = Request.ofString()
                .url(endpoint + "publickey")
                .method(Method.GET)
                .handleErrorStream()
                .executeSync();
        if (response.hasException()) {
            throw response.exception();
        }
        if (response.getStatusCode() != 200) {
            throw new WebRequestException(new Exception("Unexpected response code: " + response.getStatusCode()));
        }

        return CryptUtils.convertPKCS1ToPublicKey(response.get());
    }

    public Map<String, InetAddress> getNodes() throws WebRequestException {
        Response<String> req = Request.ofString()
                .method(Method.GET)
                .url(endpoint + "nodes")
                .handleErrorStream()
                .executeSync();

        if (req.hasException()) {
            throw req.exception();
        }

        if (req.getStatusCode() != 200) {
            throw new WebRequestException(new Exception("Unexpected response code: " + req.getStatusCode()));
        }

        Type mapType = new TypeToken<Map<String, InetAddress>>() {
        }.getType();

        return gson.fromJson(req.get(), mapType);
    }

    private long TCPing(SocketAddress address) throws IOException {
        Socket socket = new Socket();
        long startTime = System.currentTimeMillis();
        //TCPing, starts at syn and ends at syn ack

        socket.connect(address, 3000);
        long endTime = System.currentTimeMillis();
        socket.close();

        return endTime - startTime;
    }

    private static class InetAddressDeserializer implements JsonDeserializer<InetAddress> {

        @Override
        public InetAddress deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return InetAddress.getByName(json.getAsString());
            } catch (UnknownHostException e) {
                throw new JsonParseException("Invalid IP address: " + json.getAsString(), e);
            }
        }
    }
}
