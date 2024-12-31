package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

import java.util.ArrayList;
import java.util.List;

public class PacketWhitelist extends Packet {

    public List<String> whitelist;

    public PacketWhitelist() {
        super(ID.WHITELIST_LIST);
    }

    @Override
    public void read(PacketBuffer buf) {
        whitelist = new ArrayList<>();
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
