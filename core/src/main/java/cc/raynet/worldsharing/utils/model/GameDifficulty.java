package cc.raynet.worldsharing.utils.model;

import net.labymod.api.Laby;

public enum GameDifficulty {

    PEACEFUL,
    EASY,
    NORMAL,
    HARD;

    public static GameDifficulty fromId(int id) {
        if (id < 0 || id >= values().length) {
            return null;
        }

        return values()[id];
    }

    public int getId() {
        return ordinal();
    }

    @Override
    public String toString() {
        return Laby.labyAPI().minecraft().getTranslation("options.difficulty." + this.name().toLowerCase());
    }

}
