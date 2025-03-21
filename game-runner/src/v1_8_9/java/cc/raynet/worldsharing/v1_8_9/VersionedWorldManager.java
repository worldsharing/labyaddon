package cc.raynet.worldsharing.v1_8_9;

import cc.raynet.worldsharing.utils.Utils;
import cc.raynet.worldsharing.utils.WorldManager;
import cc.raynet.worldsharing.utils.model.GameDifficulty;
import cc.raynet.worldsharing.utils.model.GameMode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalChannel;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings.GameType;

import javax.inject.Singleton;
import java.io.IOException;

@Singleton
@Implements(WorldManager.class)
public class VersionedWorldManager implements WorldManager {

    @Override
    public void openChannel(ChannelHandler client) {
        new Bootstrap().group(NetworkSystem.eventLoops.getValue()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast("handler", client);
            }
        }).channel(LocalChannel.class).connect(Utils.proxyChannelAddress).syncUninterruptibly();
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
        return !server.shareToLAN(GameType.getByID(gamemode.getId()), allowCheats).equals("-1");
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
        server.setDifficultyForAllWorlds(EnumDifficulty.getDifficultyEnum(difficulty.getId()));
    }

    @Override
    public GameDifficulty getDifficulty() {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        return GameDifficulty.fromId(server.getDifficulty().getDifficultyId());
    }

    @Override
    public boolean cheatsEnabled() {
        IntegratedServer server = getServer();
        if (server == null) {
            return false;
        }
        return server.getConfigurationManager().commandsAllowedForAll;
    }

    @Override
    public void setCheatsEnabled(boolean enabled) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        server.getConfigurationManager().commandsAllowedForAll = enabled;
    }

    @Override
    public void kickPlayer(String name, String reason) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        EntityPlayerMP profile = server.getConfigurationManager().getPlayerByUsername(name);
        if (profile == null) return;
        profile.playerNetServerHandler.kickPlayerFromServer(reason);
    }

    @Override
    public int getSlots() {
        IntegratedServer server = getServer();
        return server == null ? 0 : server.getConfigurationManager().maxPlayers;
    }

    @Override
    public void setSlots(int slots) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        server.getConfigurationManager().maxPlayers = slots;
    }

    @Override
    public void stopServer() {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        server.getNetworkSystem().terminateEndpoints();
        server.isPublic = false;

    }

    @Override
    public GameMode getPlayerGameMode(String username) {
        IntegratedServer server = getServer();
        if (server == null) {
            return null;
        }
        EntityPlayerMP player = server.getConfigurationManager().getPlayerByUsername(username);
        if (player == null) {
            return null;
        }
        return GameMode.fromId(player.theItemInWorldManager.getGameType().getID());
    }

    @Override
    public void setPlayerGameMode(String username, GameMode gameMode) {
        IntegratedServer server = getServer();
        if (server == null) {
            return;
        }
        EntityPlayerMP player = server.getConfigurationManager().getPlayerByUsername(username);
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
        EntityPlayerMP player = server.getConfigurationManager().getPlayerByUsername(username);
        if (player != null) {
            if (op) {
                server.getConfigurationManager().addOp(player.getGameProfile());
            } else {
                server.getConfigurationManager().removeOp(player.getGameProfile());
            }
        }
    }

    private IntegratedServer getServer() {
        return Minecraft.getMinecraft().getIntegratedServer();
    }
}
