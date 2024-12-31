package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

public class PacketEncryptionStart extends Packet {

    public String username;

    public PacketEncryptionStart() {
        super(ID.LOGIN); // double use of ID.LOGIN
    }

    public PacketEncryptionStart(String username) {
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

    }
}
