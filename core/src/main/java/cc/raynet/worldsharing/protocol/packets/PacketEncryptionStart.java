package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketEncryptionStart extends Packet {

    public String username;

    public PacketEncryptionStart() {}

    public PacketEncryptionStart(String username) {
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

    }
}
