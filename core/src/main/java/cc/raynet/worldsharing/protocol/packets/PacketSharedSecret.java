package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

public class PacketSharedSecret extends Packet {

    private final byte[] key;

    public PacketSharedSecret() {
        this(null);
    }

    public PacketSharedSecret(byte[] key) {
        this.key = key;
    }

    @Override
    public void read(PacketBuffer buf) {

    }

    @Override
    public void write(PacketBuffer buf) {
        if (key == null) {
            return;
        }
        buf.writeBytes(key);
    }

    @Override
    public void handle(PacketHandler handler) {
    }
}
