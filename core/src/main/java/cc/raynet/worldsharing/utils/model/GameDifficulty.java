package cc.raynet.worldsharing.utils.model;

import net.labymod.api.Laby;

public enum GameDifficulty {

    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    private final int id;
    private final String name;

    GameDifficulty(int id, String name) {
        this.id = id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return Laby.labyAPI().minecraft().getTranslation("options.difficulty." + name);
    }

}
