package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketRequestTunnel extends Packet {

    public String target;
    public byte[] publicKey;

    @Override
    public void read(PacketBuffer buf) {
        target = buf.readString();
        publicKey = buf.readByteArray();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(target);
        buf.writeByteArray(publicKey);
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handle(this);
    }

}
