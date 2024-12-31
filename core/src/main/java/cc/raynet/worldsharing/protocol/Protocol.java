package cc.raynet.worldsharing.protocol;

import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.packets.*;
import cc.raynet.worldsharing.protocol.types.ID;

import java.util.HashMap;
import java.util.Map;

public class Protocol {

    private final Map<ID, Class<? extends Packet>> packets = new HashMap<>();

    public Protocol() {
        register(ID.LOGIN, PacketLogin.class);
        register(ID.ENCRYPTION_REQUEST, PacketEncryptionRequest.class);
        register(ID.TUNNEL_INFO, PacketTunnelInfo.class);
        register(ID.ERROR, PacketError.class);
        register(ID.PING, PacketPing.class);
        register(ID.PONG, PacketPong.class);
        register(ID.READY, PacketReady.class);
        register(ID.REQUEST_TUNNEL, PacketRequestTunnel.class);
        register(ID.KICK_PLAYER, PacketKickPlayer.class);
    }

    private void register(ID id, Class<? extends Packet> clazz) {
        packets.put(id, clazz);
    }

    public Packet getPacket(ID id) {
        Class<? extends Packet> packetClass = packets.get(id);
        if (packetClass != null) {
            try {
                return packetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Packet getPacket(int id) {
        return getPacket(ID.from(id));
    }

}
