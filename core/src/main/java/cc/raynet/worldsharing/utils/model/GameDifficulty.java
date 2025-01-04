package cc.raynet.worldsharing.utils.model;

import net.labymod.api.Laby;

public enum GameDifficulty {

    PEACEFUL(0),
    EASY(1),
    NORMAL(2),
    HARD(3);

    private final int id;

    GameDifficulty(int id) {
        this.id = id;
    }

    public static GameDifficulty fromId(int id) {
        for (GameDifficulty difficulty : values()) {
            if (difficulty.id == id) {
                return difficulty;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return Laby.labyAPI().minecraft().getTranslation("options.difficulty." + this.name().toLowerCase());
    }

}
