package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;

import java.util.HashSet;
import java.util.Set;

public class PacketWhitelist extends Packet {

    public Set<String> whitelist;

    @Override
    public void read(PacketBuffer buf) {
        whitelist = new HashSet<>();
        int length = buf.readInt();
        for (int i = 0; i < length; i++) {
            whitelist.add(buf.readString());
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeInt(whitelist.size());
        for (String s : whitelist) {
            buf.writeString(s);
        }
    }

    @Override
    public void handle(PacketHandler handler) {

    }
}
