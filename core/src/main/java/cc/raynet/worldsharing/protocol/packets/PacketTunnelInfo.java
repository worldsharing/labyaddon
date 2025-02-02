package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.model.TunnelInfo;
import cc.raynet.worldsharing.utils.model.WorldVisibility;

public class PacketTunnelInfo extends Packet {

    public TunnelInfo tunnelInfo;

    public PacketTunnelInfo() {

    }

    public PacketTunnelInfo(TunnelInfo tunnelInfo) {
        this.tunnelInfo = tunnelInfo;
    }

    @Override
    public void read(PacketBuffer buf) {
        this.tunnelInfo = new TunnelInfo();
        tunnelInfo.hostname = buf.readString();
        tunnelInfo.key = buf.readString();
        tunnelInfo.visibility = WorldVisibility.fromValue(buf.readByte());
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeString(tunnelInfo.hostname);
        buf.writeString(tunnelInfo.key);
        buf.writeByte(tunnelInfo.visibility.ordinal());
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handle(this);
    }
}
