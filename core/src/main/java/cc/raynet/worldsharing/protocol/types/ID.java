package cc.raynet.worldsharing.protocol.types;

public enum ID {

    UNKNOWN(-1),
    SHARED_SECRET(0),
    READY(4),
    PING(5),
    PONG(6),
    DISCONNECT(7),
    ERROR(8),
    REQUEST_TUNNEL(18),
    TUNNEL_INFO(19),
    INIT_QUIC(26),
    KICK_PLAYER(34),
    MESSAGE(35), //laby only
    LOGIN(40),
    ENCRYPTION_REQUEST(41),
    ENCRYPTION_RESPONSE(42),
    UPDATE_VISIBILITY(43),
    WHITELIST_ADD(44),
    WHITELIST_REMOVE(45),
    WHITELIST_LIST(46);

    public final int value;


    ID(int v) {
        this.value = v;
    }

    public static ID from(int i) {
        for (ID id : ID.values()) {
            if (id.value == i) {
                return id;
            }
        }
        return ID.UNKNOWN;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
