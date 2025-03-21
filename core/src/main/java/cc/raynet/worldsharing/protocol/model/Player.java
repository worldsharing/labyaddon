package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.utils.WorldManager;
import cc.raynet.worldsharing.utils.model.GameMode;
import net.luminis.quic.QuicStream;

public class Player {

    public String username;
    public int version;
    public QuicStream quicStream;
    public boolean isBedrock;
    public String nodeIP;
    public GameMode gameMode;
    public boolean operator;

    public Player() {
        WorldManager manager = WorldsharingAddon.INSTANCE.manager();
        this.gameMode = manager != null ? manager.getGameMode() : GameMode.SURVIVAL;
    }

    public Player(String username, QuicStream stream, boolean isBedrock, String nodeIP) {
        this();
        this.username = username;
        this.version = 0;
        this.isBedrock = isBedrock;
        this.quicStream = stream;
        this.nodeIP = nodeIP;
    }

}
