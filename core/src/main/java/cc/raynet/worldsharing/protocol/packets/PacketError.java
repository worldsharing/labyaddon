package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketError extends Packet {

    public String error;

    public PacketError() {}

    public PacketError(String error) {
        this.error = error;
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
