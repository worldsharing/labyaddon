package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.utils.model.GameDifficulty;
import cc.raynet.worldsharing.utils.model.GameMode;
import io.netty.channel.ChannelHandler;

import static cc.raynet.worldsharing.utils.Utils.warnUnimplemented;

public interface VersionBridge {

    default void openChannel(ChannelHandler client) {
        warnUnimplemented();
    }

    default int getSuitableLanPort() {
        warnUnimplemented();
        return -1;
    }

    default boolean publishLanWorld(int port, GameMode gamemode, boolean allowCheats) {
        warnUnimplemented();
        return false;
    }

    default boolean isPublished() {
        warnUnimplemented();
        return false;
    }

    default String getWorldName() {
        warnUnimplemented();
        return null;
    }

    default void changeGameMode(GameMode gameMode) {
        warnUnimplemented();
    }

    default GameMode getGameMode() {
        warnUnimplemented();
        return null;
    }

    default void changeDifficulty(GameDifficulty difficulty) {
        warnUnimplemented();
    }

    default GameDifficulty getDifficulty() {
        warnUnimplemented();
        return null;
    }

    default void setCheatsEnabled(boolean enabled) {
        warnUnimplemented();
    }

    default boolean cheatsEnabled() {
        warnUnimplemented();
        return false;
    }

    default void kickPlayer(String name, String reason) {
        warnUnimplemented();
    }

    default int getSlots() {
        warnUnimplemented();
        return -1;
    }

    default void setSlots(int slots) {
        warnUnimplemented();
    }

    default void stopServer() {
        warnUnimplemented();
    }

}
