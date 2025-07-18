package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.AuthType;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketAuthStart extends Packet {

    public AuthType authType;
    public String content;

    public PacketAuthStart() {}

    public PacketAuthStart(AuthType authType, String content) {
        this.authType = authType;
        this.content = content;
    }

    @Override
    public void read(PacketBuffer buf) {
        authType = AuthType.values()[buf.readByte()];
        content = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeByte(authType.ordinal());
        buf.writeString(content);
    }

    @Override
    public void handle(PacketHandler handler) {

    }
}
