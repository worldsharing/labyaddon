package cc.raynet.worldsharing.protocol.tunnel;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.SessionHandler;
import cc.raynet.worldsharing.protocol.model.Player;
import cc.raynet.worldsharing.protocol.packets.PacketTunnelRequest;
import cc.raynet.worldsharing.utils.CryptUtils;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractTunnel {

    @NotNull public static Type preferredType = Type.NETTY;
    static final String applicationProtocol = "raynettunnel";

    @NotNull private final SessionHandler sessionHandler;
    @NotNull public final PacketTunnelRequest tunnelRequest;
    @NotNull public final Type usedType;
    public boolean orphaned = false;
    public boolean closeNextTime = false;

    AbstractTunnel(@NotNull SessionHandler sessionHandler, @NotNull PacketTunnelRequest tunnelRequest, @NotNull Type type) {
        this.sessionHandler = Objects.requireNonNull(sessionHandler);
        this.tunnelRequest = Objects.requireNonNull(tunnelRequest);
        this.usedType = type;
    }

    public static void shutdown(SessionHandler sessionHandler) {
        for (var tunnel : sessionHandler.tunnels.values()) {
            tunnel.close();
        }
        NettyTunnel.shutdown();
        sessionHandler.tunnels.clear();
    }

    abstract void close();

    final Runnable handleMetadata(ByteBuf buf) throws IOException {
        boolean isBedrock = buf.readBoolean();

        if (!buf.readBoolean()) { // isLogin Header
            throw new IllegalStateException("Invalid Metadata");
        }
        final Player player = new Player(PacketBuffer.readSerializedString(buf), isBedrock, this);

        sessionHandler.players.add(player);
        sessionHandler.addon.dashboardActivity.reloadDashboard();

        closeNextTime = false;

        return () -> {
            sessionHandler.players.remove(player);
            sessionHandler.addon.manager().setOperator(player.name, false);
            sessionHandler.addon.dashboardActivity.reloadDashboard();


            Iterator<Map.Entry<String, AbstractTunnel>> iterator = sessionHandler.tunnels.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, AbstractTunnel> entry = iterator.next();
                AbstractTunnel value = entry.getValue();

                boolean found = false;
                for (var player1 : sessionHandler.players) {
                    if (player1.tunnel == value) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (!value.closeNextTime) {
                        value.closeNextTime = true;
                        continue;
                    }
                    value.close();
                    iterator.remove();
                }
            }

        };
    }

    final byte[] getPayload() throws Exception {
        return CryptUtils.encryptWithPublicKey(sessionHandler.tunnelInfo.key.getBytes(StandardCharsets.UTF_8), CryptUtils.decodePKIXPublicKey(tunnelRequest.publicKey, "RSA"));
    }

    public enum Type {
        NETTY {
            @Override public AbstractTunnel init(SessionHandler s, PacketTunnelRequest t) {
                return new NettyTunnel(s, t);
            }
        },
        KWIK {
            @Override public AbstractTunnel init(SessionHandler s, PacketTunnelRequest t) throws Exception {
                return new KwikTunnel(s, t);
            }
        };
        public abstract AbstractTunnel init(SessionHandler s, PacketTunnelRequest t) throws Exception;
    }
}
