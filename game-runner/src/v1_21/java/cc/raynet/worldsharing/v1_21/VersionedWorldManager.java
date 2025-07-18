package cc.raynet.worldsharing.v1_21;

import cc.raynet.worldsharing.utils.Utils;
import cc.raynet.worldsharing.utils.WorldManager;
import cc.raynet.worldsharing.utils.model.GameDifficulty;
import cc.raynet.worldsharing.utils.model.GameMode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalChannel;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Singleton
@Implements(WorldManager.class)
public class VersionedWorldManager implements WorldManager {

    @Override
    public void openChannel(Consumer<Channel> consumer, CompletableFuture<Channel> future) {
        new Bootstrap().group(ServerConnectionListener.SERVER_EVENT_GROUP.get()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                if (future != null && !future.isDone()) {
                    future.complete(ch);
                }
                if (consumer != null) {
                    consumer.accept(ch);
                }
            }
        }).channel(LocalChannel.class).connect(Utils.proxyChannelAddress).syncUninterruptibly();
    }

    @Override
    public int getSuitableLanPort() {
        return HttpUtil.getAvailablePort();
    }

    @Override
    public boolean publishLanWorld(int port, GameMode gamemode, boolean allowCheats) {
        IntegratedServer server = getServer();
        if (server == null) {
            return false;
        }
        return server.publishServer(GameType.byId(gamemode.getId()), allowCheats, port);
    }

    @Override
    public String getWorldName() {
        IntegratedServer server = getServer();
        if (server == null) {
            return "";
        }
        return server.getWorldData().getLevelName();
    }

    @Override
    public boolean isPublished() {
        IntegratedServer server = getServer();
        if (server == null) {
            return false;
        }
        return server.isPublished();
    }

    @Override
    public void changeGameMode(GameMode gameMode) {
        if (!isPublished()) {
            return;
        }
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        server.setDefaultGameType(GameType.byId(gameMode.getId()));
    }

    @Override
    public GameMode getGameMode() {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        return GameMode.fromId(server.getDefaultGameType().getId());
    }

    @Override
    public void changeDifficulty(GameDifficulty difficulty) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        server.setDifficulty(Difficulty.byId(difficulty.getId()), true);
    }

    @Override
    public GameDifficulty getDifficulty() {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        return GameDifficulty.fromId(server.getWorldData().getDifficulty().getId());
    }

    @Override
    public boolean cheatsEnabled() {
        IntegratedServer server = getServer();
        if (server == null) {
            return false;
        }
        return server.getPlayerList().isAllowCommandsForAllPlayers();
    }

    @Override
    public void setCheatsEnabled(boolean enabled) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        PlayerList playerList = server.getPlayerList();
        playerList.setAllowCommandsForAllPlayers(enabled);
        for (var pl : playerList.getPlayers()) {
            playerList.sendPlayerPermissionLevel(pl);
        }
    }

    @Override
    public void kickPlayer(String name, String reason) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        ServerPlayer profile = server.getPlayerList().getPlayerByName(name);
        if (profile == null) {
            return;
        }
        profile.connection.disconnect(Component.literal(reason));
    }

    @Override
    public int getSlots() {
        IntegratedServer server = getServer();
        if (server == null) {
            return 0;
        }
        return server.getPlayerList().maxPlayers;
    }

    @Override
    public void setSlots(int slots) {
        IntegratedServer server = getServer();
        if (server != null) {
            server.getPlayerList().maxPlayers = slots;
        }
    }

    @Override
    public void stopServer() {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        server.getConnection().stop();
        server.publishedPort = -1;
    }

    @Override
    public GameMode getPlayerGameMode(String username) {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        ServerPlayer player = server.getPlayerList().getPlayerByName(username);
        if (player == null) {
            return null;
        }
        return GameMode.fromId(player.gameMode.getGameModeForPlayer().getId());
    }

    @Override
    public void setPlayerGameMode(String username, GameMode gameMode) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayerByName(username);
        if (player == null) {
            return;
        }

        player.setGameMode(GameType.byId(gameMode.getId()));
    }

    @Override
    public void setOperator(String username, boolean op) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayerByName(username);
        if (player != null) {
            if (op) {
                server.getPlayerList().op(player.getGameProfile());
            } else {
                server.getPlayerList().deop(player.getGameProfile());
            }
        }
    }

    private IntegratedServer getServer() {
        return Minecraft.getInstance().getSingleplayerServer();
    }
}
