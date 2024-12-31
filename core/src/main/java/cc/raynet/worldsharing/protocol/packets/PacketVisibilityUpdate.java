package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;
import cc.raynet.worldsharing.utils.model.WorldVisibility;

public class PacketVisibilityUpdate extends Packet {

    private byte visibility;

    public PacketVisibilityUpdate() {
        super(ID.UPDATE_VISIBILITY);
    }

    public PacketVisibilityUpdate(WorldVisibility visibility) {
        this(visibility.value);
    }

    public PacketVisibilityUpdate(byte visibility) {
        this();
        this.visibility = visibility;
    }

    public WorldVisibility getWorldVisibility() {
        return WorldVisibility.fromValue(visibility);
    }

    @Override
    public void read(PacketBuffer buf) {
        visibility = buf.readByte();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeByte(visibility);
    }

    @Override
    public void handle(PacketHandler handler) {
    }
}
