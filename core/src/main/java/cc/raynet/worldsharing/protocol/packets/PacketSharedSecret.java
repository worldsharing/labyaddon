package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

public class PacketSharedSecret extends Packet {

    private final byte[] encryptedSharedSecret;

    public PacketSharedSecret() {
        this(null);
    }

    public PacketSharedSecret(byte[] encryptedSharedSecret) {
        super(ID.SHARED_SECRET);
        this.encryptedSharedSecret = encryptedSharedSecret;
    }

    @Override
    public void read(PacketBuffer buf) {

    }

    @Override
    public void write(PacketBuffer buf) {
        if (encryptedSharedSecret == null) {
            return;
        }
        //write fully packet encoded encrypted sharedsecret
        buf.writeBytes(encryptedSharedSecret);
    }

    @Override
    public void handle(PacketHandler handler) {
    }
}
