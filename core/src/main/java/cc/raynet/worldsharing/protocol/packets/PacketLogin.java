package cc.raynet.worldsharing.protocol.packets;

import cc.raynet.worldsharing.protocol.PacketBuffer;
import cc.raynet.worldsharing.protocol.model.Packet;
import cc.raynet.worldsharing.protocol.model.PacketHandler;
import cc.raynet.worldsharing.protocol.types.ID;

import java.util.HashMap;
import java.util.Map;

public class PacketLogin extends Packet {

    public byte visibility;
    public String version;
    public int mcVersion;
    public String worldName;
    public int maxPlayers;
    public String[] friends;

    public PacketLogin() {
        super(ID.LOGIN);
    }

    public PacketLogin(byte visibility, String version, int mcVersion, String worldName, int maxPlayers, String[] friends) {
        this();
        this.visibility = visibility;
        this.version = version;
        this.mcVersion = mcVersion;
        this.worldName = worldName;
        this.maxPlayers = maxPlayers;
        this.friends = friends;
    }

    @Override
    public void read(PacketBuffer buf) {
        visibility = buf.readByte();
        version = buf.readString();
        mcVersion = buf.readInt();
        worldName = buf.readString();
        maxPlayers = buf.readInt();

        int lFriends = buf.readInt();
        friends = new String[lFriends];
        for (int i = 0; i < lFriends; i++) {
            friends[i] = buf.readString();
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeByte(visibility);
        buf.writeString(version);
        buf.writeInt(mcVersion);
        buf.writeString(worldName);
        buf.writeInt(maxPlayers);

        buf.writeInt(friends == null ? 0 : friends.length);
        if (friends != null) {
            for (String friend : friends) {
                buf.writeString(friend);
            }
        }
    }

    @Override
    public void handle(PacketHandler handler) {
    }

}
