package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketPing extends Packet {

    @Override
    public void read(PacketBuffer buf) {
    }

    @Override
    public void write(PacketBuffer buf) {
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handle(this);
    }
}
