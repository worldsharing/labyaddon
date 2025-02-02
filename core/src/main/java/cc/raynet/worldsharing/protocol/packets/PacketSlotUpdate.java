package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketSlotUpdate extends Packet {

    private int slots;

    public PacketSlotUpdate() {}

    public PacketSlotUpdate(int slots) {
        this.slots = slots;
    }

    @Override
    public void read(PacketBuffer buf) {
        slots = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeInt(slots);
    }

    @Override
    public void handle(PacketHandler handler) {

    }
}
