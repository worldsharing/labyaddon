package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

public class PacketError extends Packet {

    public String error;

    public PacketError() {
        super(ID.ERROR);
    }

    @Override
    public void read(PacketBuffer buf) {
        error = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(error);
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handle(this);
    }

}
