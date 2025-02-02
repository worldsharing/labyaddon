package cc.raynet.worldsharing.v1_12_2.client;

import cc.raynet.worldsharing.utils.VersionBridge;
import cc.raynet.worldsharing.utils.VersionStorage;
import cc.raynet.worldsharing.utils.model.GameDifficulty;
import cc.raynet.worldsharing.utils.model.GameMode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;

import java.io.IOException;

public class VersionBridgeImpl implements VersionBridge {

    @Override
    public void openChannel(ChannelHandler client) {
        new Bootstrap().group(NetworkSystem.SERVER_NIO_EVENTLOOP.getValue()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast("handler", client);
            }
        }).channel(LocalChannel.class).connect(VersionStorage.proxyChannelAddress).syncUninterruptibly();
    }

    @Override
    public int getSuitableLanPort() {
        try {
            return HttpUtil.getSuitableLanPort();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public boolean publishLanWorld(int port, GameMode gamemode, boolean allowCheats) {
        IntegratedServer server = getServer();
        if (server == null) {
            return false;
        }
        return !server
                .shareToLAN(GameType.getByID(gamemode.getId()), allowCheats)
                .equals("-1");
    }

    @Override
    public String getWorldName() {
        IntegratedServer server = getServer();
        if (server == null) {
            return "";
        }
        return server.getWorldName();
    }

    @Override
    public boolean isPublished() {
        IntegratedServer server = getServer();
        if (server == null) {
            return false;
        }
        return server.getPublic();
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
        server.setGameType(GameType.getByID(gameMode.getId()));
    }

    @Override
    public GameMode getGameMode() {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        return GameMode.fromId(server.getGameType().getID());
    }

    @Override
    public void changeDifficulty(GameDifficulty difficulty) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        server.setDifficultyForAllWorlds(EnumDifficulty.byId(difficulty.getId()));
    }

    @Override
    public GameDifficulty getDifficulty() {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        return GameDifficulty.fromId(server.getDifficulty().getId());
    }

    @Override
    public boolean cheatsEnabled() {
        IntegratedServer server = getServer();
        if (server == null) {
            return false;
        }
        return server.getPlayerList().commandsAllowedForAll;
    }

    @Override
    public void setCheatsEnabled(boolean enabled) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        PlayerList playerList = server.getPlayerList();
        playerList.commandsAllowedForAll = enabled;
        for (var pl : playerList.getPlayers()) {
            playerList.updatePermissionLevel(pl);
        }
    }

    @Override
    public void kickPlayer(String name, String reason) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        EntityPlayerMP profile = server
                .getPlayerList()
                .getPlayerByUsername(name);
        if (profile == null) return;
        profile.connection.disconnect(new TextComponentString(reason));
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
        if (server == null) {
            return;
        }
        server.getPlayerList().maxPlayers = slots;
    }

    @Override
    public void stopServer() {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        if (server.getServer().getNetworkSystem() != null) {
            server.getServer().getNetworkSystem().terminateEndpoints();
        }
        server.isPublic = false;
    }

    @Override
    public GameMode getPlayerGameMode(String username) {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(username);
        if (player == null) {
            return null;
        }
        return GameMode.fromId(player.interactionManager.getGameType().getID());
    }

    @Override
    public void setPlayerGameMode(String username, GameMode gameMode) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(username);
        if (player == null) {
            return;
        }
        player.setGameType(GameType.getByID(gameMode.getId()));
    }

    @Override
    public void setOperator(String username, boolean op) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(username);
        if (player != null) {
            if (op) {
                server.getPlayerList().addOp(player.getGameProfile());
            } else {
                server.getPlayerList().removeOp(player.getGameProfile());
            }
        }
    }

    private IntegratedServer getServer() {
        return Minecraft.getMinecraft().getIntegratedServer();
    }
}
