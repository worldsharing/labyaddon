package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

public class PacketPong extends Packet {

    public PacketPong() {
        super(ID.PONG);
    }

    @Override
    public void read(PacketBuffer buf) {
    }

    @Override
    public void write(PacketBuffer buf) {
    }

    @Override
    public void handle(PacketHandler handler) {
    }
}
