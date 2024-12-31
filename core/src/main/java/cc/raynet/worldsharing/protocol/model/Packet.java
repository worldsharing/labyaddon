package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.types.ID;

public abstract class Packet {

    private final int id;

    public Packet(int id) {
        this.id = id;
    }

    public Packet(ID id) {
        this(id.value);
    }

    public int getID() {
        return id;
    }

    public abstract void read(PacketBuffer buf);

    public abstract void write(PacketBuffer buf);

    public abstract void handle(PacketHandler handler);
}
