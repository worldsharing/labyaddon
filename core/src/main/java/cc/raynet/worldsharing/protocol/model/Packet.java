package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.protocol.PacketBuffer;

public abstract class Packet {

    public abstract void read(PacketBuffer buf);

    public abstract void write(PacketBuffer buf);

    public abstract void handle(PacketHandler handler);
}
