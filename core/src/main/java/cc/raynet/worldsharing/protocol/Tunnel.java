package cc.raynet.worldsharing.protocol;


import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.model.Player;
import cc.raynet.worldsharing.protocol.model.TunnelRequest;
import cc.raynet.worldsharing.protocol.proxy.ChannelProxy;
import cc.raynet.worldsharing.protocol.types.ID;
import cc.raynet.worldsharing.protocol.types.VarInt;
import cc.raynet.worldsharing.serverlist.WorldsServerTab;
import cc.raynet.worldsharing.utils.CryptUtils;
import cc.raynet.worldsharing.utils.Utils;
import cc.raynet.worldsharing.utils.VersionStorage;
import net.labymod.api.util.Pair;
import net.luminis.quic.QuicClientConnection;
import net.luminis.quic.QuicStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Tunnel {

    private final SessionHandler sessionHandler;
    private final QuicClientConnection connection;

    public Tunnel(SessionHandler sessionHandler, TunnelRequest t) throws Exception {
        this.sessionHandler = sessionHandler;

        if (sessionHandler.tunnels.containsKey(t.target)) {
            QuicClientConnection temp = sessionHandler.tunnels.get(t.target).getFirst();
            if (temp != null && temp.isConnected()) {
                temp.close();
            }
            sessionHandler.tunnels.remove(t.target);
        }

        connection = QuicClientConnection.newBuilder()
                .uri(URI.create("udp://" + t.target))
                .applicationProtocol("raynettunnel")
                .noServerCertificateCheck()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        connection.connect();
        connection.keepAlive(10);

        sessionHandler.tunnels.put(t.target, Pair.of(connection, false));

        QuicStream authStream = connection.createStream(true);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        VarInt.writeToStream(buffer, VarInt.size(ID.INIT_QUIC.value) + sessionHandler.tunnelInfo.key.length());
        VarInt.writeToStream(buffer, ID.INIT_QUIC.value);
        buffer.writeBytes(sessionHandler.tunnelInfo.key.getBytes(StandardCharsets.UTF_8));
        buffer.flush();

        authStream.getOutputStream()
                .write(CryptUtils.encryptWithPublicKey(buffer.toByteArray(), CryptUtils.decodePKIXPublicKey(t.publicKey, "RSA")));

        try {
            while (connection.isConnected()) {
                if (authStream.getInputStream().read() > 0) {
                    Thread.ofVirtual().start(this::proxy);
                }
            }
        } catch (Exception e) {
            if (!connection.isConnected() && !e.getMessage().equals("Connection closed")) {
                WorldsharingAddon.LOGGER.error("failed to read from Control: {}", e.getMessage());
            }
        } finally {
            sessionHandler.tunnels.remove(t.target);
        }
    }

    private void proxy() {
        try {
            QuicStream stream = connection.createStream(true);

            if (stream == null) {
                throw new RuntimeException("Stream is null");
            }
            stream.getOutputStream().write(0xA);

            boolean isBedrock = stream.getInputStream().read() == 1;

            if (stream.getInputStream().read() == 1) {
                final Player player = new Player(Utils.DeserializeString(stream.getInputStream()), stream, isBedrock, connection.getServerAddress()
                        .getAddress()
                        .getHostAddress());
                sessionHandler.players.add(player);
                if (player.isBedrock) {
                    sessionHandler.addon.bedrockPlayers.add(player.username);
                }

                if (VersionStorage.bridge != null) {
                    VersionStorage.bridge.openChannel(new ChannelProxy(stream, () -> {
                        sessionHandler.players.remove(player);
                        sessionHandler.addon.dashboardActivity.reloadDashboard();
                    }));
                    sessionHandler.addon.dashboardActivity.reloadDashboard();
                }
            }
        } catch (IOException e) {
            WorldsharingAddon.LOGGER.warn("proxy failed: {}", e.getMessage());
        }
    }
}
