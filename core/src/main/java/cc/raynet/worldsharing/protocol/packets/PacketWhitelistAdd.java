package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketWhitelistAdd extends Packet {

    public String username;
    public byte type;

    public PacketWhitelistAdd() {}

    public PacketWhitelistAdd(String username, byte type) {
        this.username = username;
        this.type = type;
    }

    @Override
    public void read(PacketBuffer buf) {
        this.type = buf.readByte();
        this.username = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeByte(type);
        buf.writeString(username);
    }

    @Override
    public void handle(PacketHandler handler) {
    }
}
