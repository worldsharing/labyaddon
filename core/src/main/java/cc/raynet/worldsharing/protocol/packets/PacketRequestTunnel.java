package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.model.TunnelRequest;
import cc.raynet.worldsharing.protocol.types.ID;

public class PacketRequestTunnel extends Packet {

    public TunnelRequest tunnelRequest;

    public PacketRequestTunnel() {
        super(ID.REQUEST_TUNNEL);
        tunnelRequest = new TunnelRequest();
    }

    @Override
    public void read(PacketBuffer buf) {
        tunnelRequest.target = buf.readString();
        tunnelRequest.publicKey = buf.readByteArray();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(tunnelRequest.target);
        buf.writeByteArray(tunnelRequest.publicKey);
    }

    @Override
    public void handle(PacketHandler handler) {
        if (tunnelRequest == null) {
            return;
        }
        handler.handle(this);
    }

}
