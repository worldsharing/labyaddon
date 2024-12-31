package cc.raynet.worldsharing.utils.model;

import net.labymod.api.Laby;
import net.labymod.api.util.CharSequences;

public enum GameMode {

    SURVIVAL(0),
    CREATIVE(1),
    ADVENTURE(2),
    SPECTATOR(3);

    private final int id;
    private final String name;

    GameMode(int id) {
        this.id = id;
        String name = this.name();
        this.name = CharSequences.toString(CharSequences.capitalize(name.toLowerCase()));
    }

    public static GameMode fromId(int id) {
        for (GameMode mode : GameMode.values()) {
            if (mode.getId() == id) {
                return mode;
            }
        }
        return null;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return Laby.labyAPI().minecraft().getTranslation("selectWorld.gameMode." + name.toLowerCase()).split(" ")[0];
    }
}
