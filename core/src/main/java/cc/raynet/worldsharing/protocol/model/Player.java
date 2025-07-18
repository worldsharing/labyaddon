package cc.raynet.worldsharing.protocol.model;

import cc.raynet.worldsharing.WorldsharingAddon;
import cc.raynet.worldsharing.protocol.tunnel.AbstractTunnel;
import cc.raynet.worldsharing.utils.WorldManager;
import cc.raynet.worldsharing.utils.model.GameMode;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;

public class Player {

    public final String name;
    public final boolean isBedrock;
    public GameMode gameMode;
    public boolean operator;
    public AbstractTunnel tunnel;

    public Player(String name, boolean isBedrock, AbstractTunnel tunnel) {
        this.name = name;
        this.isBedrock = isBedrock;
        WorldManager manager = WorldsharingAddon.INSTANCE.manager();
        this.gameMode = manager != null ? manager.getGameMode() : GameMode.SURVIVAL;
        this.tunnel = tunnel;
    }

    public Icon getHead() {
        return this.isBedrock ?
            Laby.labyAPI().minecraft().clientWorld().getPlayer(this.name).map(v -> Icon.head(v.skinTexture())).orElse(Icon.head(this.name)) :
            Icon.head(this.name);
    }
}
