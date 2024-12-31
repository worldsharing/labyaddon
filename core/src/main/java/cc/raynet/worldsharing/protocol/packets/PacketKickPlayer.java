package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

public class PacketKickPlayer extends Packet {

    public String username;

    public PacketKickPlayer() {
        super(ID.KICK_PLAYER);
    }

    public PacketKickPlayer(String username) {
        this();
        this.username = username;
    }

    @Override
    public void read(PacketBuffer buf) {
        username = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(username);
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handle(this);
    }
}
