package cc.raynet.worldsharing.protocol;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.api.API;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.model.Player;
import cc.raynet.worldsharing.protocol.model.TunnelInfo;
import cc.raynet.worldsharing.protocol.packets.*;
import cc.raynet.worldsharing.protocol.pipeline.ChannelHandler;
import cc.raynet.worldsharing.protocol.pipeline.PacketEncryptingDecoder;
import cc.raynet.worldsharing.protocol.pipeline.PacketEncryptingEncoder;
import cc.raynet.worldsharing.protocol.types.ConnectionState;
import cc.raynet.worldsharing.utils.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.event.ClickEvent;
import net.labymod.api.client.component.event.HoverEvent;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.concurrent.ThreadFactoryBuilder;
import net.labymod.api.util.Pair;
import net.labymod.api.util.io.web.exception.WebRequestException;
import net.luminis.quic.QuicClientConnection;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SessionHandler extends PacketHandler {

    public final WorldsharingAddon addon;
    private final NioEventLoopGroup wsLoopGroup = new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).withNameFormat("WorldsharingNio#%d")
            .build());
    private final Protocol protocol;

    public Set<Player> players = Collections.synchronizedSet(new HashSet<>());
    public Set<String> whitelistedPlayers = Collections.synchronizedSet(new HashSet<>());

    public TunnelInfo tunnelInfo;
    @Nullable public String lastError;
    public Map<String, Pair<QuicClientConnection, Boolean>> tunnels = new ConcurrentHashMap<>();
    private ChannelHandler channelHandler = null;
    private ConnectionState state;

    public SessionHandler(WorldsharingAddon addon) {
        this.addon = addon;
        this.protocol = new Protocol();
        this.tunnelInfo = new TunnelInfo();
        this.state = ConnectionState.DISCONNECTED;
    }

    public Protocol getPacketRegistry() {
        return protocol;
    }

    public ConnectionState getState() {
        return state;
    }

    public void init() {
        if (state != ConnectionState.DISCONNECTED) {
            WorldsharingAddon.LOGGER.debug("Already connected or connecting.");
            shutdown();
            return;
        }
        Thread.ofVirtual().start(() -> {
            try {
                connect();
            } catch (Exception e) {
                if (e instanceof WebRequestException || e instanceof NoSuchAlgorithmException || e instanceof InvalidKeySpecException) {
                    disconnect("Unexpected API response");
                } else {
                    disconnect(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                }
                addon.logger().warn("failed to connect: " + e.getMessage());
            }
        });
    }

    public void disconnect() {
        disconnect(null);
        WorldManager manager = addon.manager();
        if (manager != null) {
            for (Player player : players) {
                manager.kickPlayer(player.username, "Host stopped sharing");
            }
            manager.stopServer();
        }
        players.clear();
    }

    public void disconnect(String error) {
        if (state != ConnectionState.DISCONNECTING && state != ConnectionState.DISCONNECTED) {
            this.lastError = error;
            shutdown();
        }
    }

    private void shutdown() {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.DISCONNECTING) {
            return;
        }
        state = ConnectionState.DISCONNECTING;
        NioSocketChannel channel = getChannel();
        if (channel != null && channel.isOpen()) {
            channel.close();
            AddonMessageUtil.send(AddonMessageUtil.getOnlineFriendsUUIDs(), AddonMessageUtil.ACTION_REMOVE);
        }

        channelHandler = null;

        state = ConnectionState.DISCONNECTED;
        tunnelInfo.key = "";
        tunnelInfo.hostname = "";
        whitelistedPlayers.clear();
        if (!tunnels.isEmpty()) {
            tunnels.forEach((e, v) -> v.setSecond(Boolean.TRUE));
        }
    }


    private synchronized void connect() throws Exception {
        state = ConnectionState.CONNECTING;
        addon.dashboardActivity.reloadDashboard();
        this.channelHandler = new ChannelHandler(this);

        Bootstrap bootstrap = new Bootstrap().group(this.wsLoopGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(this.channelHandler);

        API.Control control = API.getControl();

        bootstrap.connect(new InetSocketAddress(control.host, control.port)).syncUninterruptibly();

        byte[] sharedSecret = Utils.randomString(16).getBytes(StandardCharsets.UTF_8);

        sendPacket(new PacketSharedSecret(CryptUtils.encryptWithPublicKey(sharedSecret, control.key)));

        SecretKeySpec secretKey = new SecretKeySpec(sharedSecret, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(sharedSecret);

        Channel ch = getChannel();
        ch.pipeline().addBefore("splitter", "decrypt", new PacketEncryptingDecoder(CryptUtils.createCipher(Cipher.DECRYPT_MODE, secretKey, ivSpec)));
        ch.pipeline().addBefore("prepender", "encrypt", new PacketEncryptingEncoder(CryptUtils.createCipher(Cipher.ENCRYPT_MODE, secretKey, ivSpec)));

        sendPacket(new PacketEncryptionStart(Laby.labyAPI().getName()));
    }

    public boolean isConnected() {
        return state == ConnectionState.CONNECTED;
    }

    @Override
    public void handle(PacketEncryptionRequest enc) {
        byte[] bytes = CryptUtils.getServerIdHash(enc.serverID, enc.publicKey);
        if (bytes == null) {
            disconnect("failed to obtain Server ID hash");
            return;
        }

        try {
            if (!addon.labyAPI()
                    .minecraft()
                    .authenticator()
                    .joinServer(addon.labyAPI()
                            .minecraft()
                            .sessionAccessor()
                            .getSession(), new BigInteger(bytes).toString(16))
                    .get()) {
                disconnect("failed to authenticate");
                return;
            }
        } catch (ExecutionException | InterruptedException e) {
            disconnect(e.getMessage());
            return;
        }

        if (addon.manager() == null) {
            WorldsharingAddon.LOGGER.debug("WorldManager is null");
        }

        sendPacket(new PacketEncryptionResponse(enc.verifyToken), e -> sendPacket(new PacketLogin(tunnelInfo.visibility.value, addon.addonInfo()
                .getVersion(), addon.labyAPI()
                .minecraft()
                .getProtocolVersion(), addon.manager().getWorldName(), addon.manager().getSlots(), AddonMessageUtil.getFriends())));
    }

    @Override
    public void handle(PacketRequestTunnel rt) {
        if (tunnels.containsKey(rt.target)) {
            if (Boolean.FALSE.equals(tunnels.get(rt.target).getSecond())) {
                return;
            }
        }
        Thread.ofVirtual().start(() -> {
            try {
                new Tunnel(this, rt);
            } catch (Exception e) {
                lastError = "failed to create tunnel: " + e.getCause() == null ? e.getMessage() :  e.getCause().getMessage();
                addon.logger().error(lastError);
            }
        });
    }

    @Override
    public void handle(PacketTunnelInfo ti) {
        tunnelInfo = ti.tunnelInfo;

        addon.displayMessage(Component.translatable("worldsharing.messages.public_domain", NamedTextColor.GREEN)
                .argument(Component.text(tunnelInfo.hostname, NamedTextColor.WHITE)
                        .clickEvent(ClickEvent.copyToClipboard(tunnelInfo.hostname))
                        .hoverEvent(HoverEvent.showText(Component.translatable("worldsharing.messages.copy_domain", NamedTextColor.GREEN)))));
    }

    @Override
    public void handle(PacketError err) {
        WorldsharingAddon.LOGGER.error("received error packet, reason: " + err.error);
        disconnect(err.error);

    }

    @Override
    public void handle(PacketPing ping) {
        sendPacket(new PacketPong());
    }

    @Override
    public void handle(PacketDisconnect disconnect) {
        WorldsharingAddon.LOGGER.debug("received disconnect packet, reason: " + disconnect.reason);
        disconnect("Reason: " + disconnect.reason);
    }

    @Override
    public void handle(PacketReady ready) {
        state = ConnectionState.CONNECTED;
        addon.dashboardActivity.reloadDashboard();
        AddonMessageUtil.send(AddonMessageUtil.getOnlineFriendsUUIDs(), AddonMessageUtil.ACTION_ADD);
        Laby.labyAPI().minecraft().sounds().playSound(ResourceLocation.create("labymod", "lootbox.common"), 1f, 1f);
    }

    @Override
    public void handle(PacketWhitelist whitelist) {
        this.whitelistedPlayers = whitelist.whitelist;
    }

    public void sendPacket(Packet packet) {
        sendPacket(packet, null);
    }

    public void sendPacket(Packet packet, Consumer<NioSocketChannel> callback) {
        NioSocketChannel channel = getChannel();
        if (channel != null && channel.isActive()) {
            if (channel.eventLoop().inEventLoop()) {
                channel.writeAndFlush(packet);
                if (callback != null) {
                    callback.accept(channel);
                }
            } else {
                channel.eventLoop().execute(() -> {
                    channel.writeAndFlush(packet);
                    if (callback != null) {
                        callback.accept(channel);
                    }
                });
            }
        }

    }

    public NioSocketChannel getChannel() {
        return channelHandler == null ? null : this.channelHandler.getChannel();
    }

    public void manageWhitelist(String u, boolean add) {
        if (!isConnected()) {
            return;
        }
        final String username = u.toLowerCase();
        sendPacket(add ? new PacketWhitelistAdd(username, (byte) 1) : new PacketWhitelistRemove(username, (byte) 1), e -> {
            if (add) {
                whitelistedPlayers.add(username);
            } else {
                whitelistedPlayers.remove(username);
            }
        });
    }

}
