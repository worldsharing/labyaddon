package cc.raynet.worldsharing.protocol;

import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.packets.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Protocol {

    private final Map<Integer, Class<? extends Packet>> packets = new HashMap<>();

    public Protocol() {
        register(0, PacketSharedSecret.class);
        register(4, PacketReady.class);
        register(5, PacketPing.class);
        register(6, PacketPong.class);
        register(7, PacketDisconnect.class);
        register(8, PacketError.class);
        register(18, PacketRequestTunnel.class);
        register(19, PacketTunnelInfo.class);
        register(40, PacketEncryptionStart.class);
        register(41, PacketEncryptionRequest.class);
        register(42, PacketEncryptionResponse.class);
        register(43, PacketLogin.class);
        register(44, PacketVisibilityUpdate.class);
        register(45, PacketWhitelistAdd.class);
        register(46, PacketWhitelistRemove.class);
        register(47, PacketWhitelist.class);
        register(48, PacketSlotUpdate.class);
    }

    private void register(int id, Class<? extends Packet> clazz) {
        packets.put(id, clazz);
    }

    public Packet getPacket(int id) {
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

    public int getPacketId(Packet packet) {
        Iterator var2 = this.packets.entrySet().iterator();

        Map.Entry entry;
        Class clazz;
        do {
            if (!var2.hasNext()) {
                throw new RuntimeException("Packet " + packet + " is not registered.");
            }

            entry = (Map.Entry)var2.next();
            clazz = (Class)entry.getValue();
        } while(!clazz.isInstance(packet));

        return (Integer)entry.getKey();
    }

}
