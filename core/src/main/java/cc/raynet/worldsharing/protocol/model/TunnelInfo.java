package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.utils.model.WorldVisibility;

public class TunnelInfo {

    public String hostname;
    public String key;
    public WorldVisibility visibility;

    public TunnelInfo() {
        this.visibility = WorldVisibility.PUBLIC;
    }

}
