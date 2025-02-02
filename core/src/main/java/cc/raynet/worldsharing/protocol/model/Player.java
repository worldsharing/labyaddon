package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.utils.VersionStorage;
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
        this.gameMode = VersionStorage.bridge != null ? VersionStorage.bridge.getGameMode() : GameMode.SURVIVAL;
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
