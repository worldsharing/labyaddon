package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketAuthResponse extends Packet {

    public byte[] verifyToken;

    public PacketAuthResponse() {
        this(null);
    }

    public PacketAuthResponse(byte[] verifyToken) {
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
