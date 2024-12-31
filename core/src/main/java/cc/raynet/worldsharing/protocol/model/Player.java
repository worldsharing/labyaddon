package cc.raynet.worldsharing.protocol.model;

import net.luminis.quic.QuicStream;

public class Player {

    public String username;
    public int version;
    public QuicStream quicStream;
    public boolean isBedrock;
    public String nodeIP;

    public Player() {
    }

    public Player(String username, QuicStream stream, boolean isBedrock, String nodeIP) {
        this.username = username;
        this.version = 0;
        this.isBedrock = isBedrock;
        this.quicStream = stream;
        this.nodeIP = nodeIP;
    }

}
