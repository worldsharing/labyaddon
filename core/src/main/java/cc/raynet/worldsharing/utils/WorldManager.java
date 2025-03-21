package cc.raynet.worldsharing.utils;

import cc.raynet.worldsharing.utils.model.GameDifficulty;
import cc.raynet.worldsharing.utils.model.GameMode;
import io.netty.channel.ChannelHandler;
import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface WorldManager {

    void openChannel(ChannelHandler client);

    int getSuitableLanPort();

    boolean publishLanWorld(int port, GameMode gamemode, boolean allowCheats);

    boolean isPublished();

    String getWorldName();

    void changeGameMode(GameMode gameMode);

    GameMode getGameMode();

    void changeDifficulty(GameDifficulty difficulty);

    GameDifficulty getDifficulty();

    void setCheatsEnabled(boolean enabled);

    boolean cheatsEnabled();

    void kickPlayer(String name, String reason);

    int getSlots();

    void setSlots(int slots);

    void stopServer();

    GameMode getPlayerGameMode(String username);

    void setPlayerGameMode(String username, GameMode gameMode);

    void setOperator(String username, boolean op);

}
