package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketEncryptionResponse extends Packet {

    public byte[] verifyToken;

    public PacketEncryptionResponse() {
        this(null);
    }

    public PacketEncryptionResponse(byte[] verifyToken) {
        this.verifyToken = verifyToken;
    }

    @Override
    public void read(PacketBuffer buf) {
        verifyToken = buf.readByteArray();
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeByteArray(verifyToken);
    }

    @Override
    public void handle(PacketHandler handler) {

    }
}
