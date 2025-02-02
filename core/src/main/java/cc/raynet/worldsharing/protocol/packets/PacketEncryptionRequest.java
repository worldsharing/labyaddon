package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketEncryptionRequest extends Packet {

    public String serverID;
    public byte[] publicKey;
    public byte[] verifyToken;

    @Override
    public void read(PacketBuffer buf) {
        serverID = buf.readString();
        publicKey = buf.readByteArray();
        verifyToken = buf.readByteArray();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(serverID);
        buf.writeByteArray(publicKey);
        buf.writeByteArray(verifyToken);
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handle(this);
    }
}
