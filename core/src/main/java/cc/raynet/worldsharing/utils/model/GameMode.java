package cc.raynet.worldsharing.utils.model;

import net.labymod.api.Laby;
import net.labymod.api.util.CharSequences;

public enum GameMode {

    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR;

    private final String name = CharSequences.toString(CharSequences.capitalize(name().toLowerCase()));

    public static GameMode fromId(int id) {
        if (id < 0 || id >= values().length) {
            return null;
        }

        return values()[id];
    }

    public int getId() {
        return ordinal();
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return Laby.labyAPI().minecraft().getTranslation("selectWorld.gameMode." + name.toLowerCase()).split(" ")[0];
    }
}
