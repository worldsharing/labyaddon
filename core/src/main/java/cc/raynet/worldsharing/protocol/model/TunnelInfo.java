package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.utils.model.WorldVisibility;

public class TunnelInfo {

    public String hostname;
    public String key;
    public WorldVisibility visibility;

    public TunnelInfo(String hostname, String key, WorldVisibility visibility) {
        this.hostname = hostname;
        this.key = key;
        this.visibility = visibility;
    }

    public TunnelInfo(String hostname, String key) {
        this.hostname = hostname;
        this.key = key;
        this.visibility = WorldVisibility.PUBLIC;
    }

    public TunnelInfo() {
        this.visibility = WorldVisibility.PUBLIC;
    }

}
