package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

public class PacketDisconnect extends Packet {

    public String reason;

    public PacketDisconnect() {
        super(ID.DISCONNECT);
    }

    @Override
    public void read(PacketBuffer buf) {
        reason = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(reason);
    }

    @Override
    public void handle(PacketHandler handler) {

    }
}
