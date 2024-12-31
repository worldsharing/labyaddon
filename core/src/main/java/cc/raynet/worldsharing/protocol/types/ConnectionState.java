package cc.raynet.worldsharing.protocol.types;

public enum ConnectionState {
    DISCONNECTED, // if packet uses this state, it means that the packet is for all states
    DISCONNECTING,
    CONNECTING,
    CONNECTED
}
