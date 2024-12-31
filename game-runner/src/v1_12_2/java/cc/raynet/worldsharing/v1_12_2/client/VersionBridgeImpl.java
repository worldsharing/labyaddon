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

        return !Minecraft.getMinecraft()
                .getIntegratedServer()
                .shareToLAN(GameType.getByID(gamemode.getId()), allowCheats)
                .equals("-1");
    }

    @Override
    public String getWorldName() {
        return Minecraft.getMinecraft().getIntegratedServer().getWorldName();
    }

    @Override
    public boolean isPublished() {
        return Minecraft.getMinecraft().getIntegratedServer().getPublic();
    }

    @Override
    public void changeGameMode(GameMode gameMode) {
        if (!isPublished()) {
            return;
        }
        Minecraft.getMinecraft().getIntegratedServer().setGameType(GameType.getByID(gameMode.getId()));
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.fromId(Minecraft.getMinecraft().getIntegratedServer().getGameType().getID());
    }

    @Override
    public void changeDifficulty(GameDifficulty difficulty) {
        Minecraft.getMinecraft()
                .getIntegratedServer()
                .setDifficultyForAllWorlds(EnumDifficulty.byId(difficulty.getId()));
    }

    @Override
    public GameDifficulty getDifficulty() {
        return GameDifficulty.fromId(Minecraft.getMinecraft().getIntegratedServer().getDifficulty().getId());
    }

    @Override
    public boolean cheatsEnabled() {
        return Minecraft.getMinecraft().getIntegratedServer().getPlayerList().commandsAllowedForAll;
    }

    @Override
    public void setCheatsEnabled(boolean enabled) {
        PlayerList playerList = Minecraft.getMinecraft().getIntegratedServer().getPlayerList();
        playerList.commandsAllowedForAll = enabled;
        for (var pl : playerList.getPlayers()) {
            playerList.updatePermissionLevel(pl);
        }
    }

    @Override
    public void kickPlayer(String name, String reason) {
        EntityPlayerMP profile = Minecraft.getMinecraft()
                .getIntegratedServer()
                .getServer()
                .getPlayerList()
                .getPlayerByUsername(name);
        if (profile == null) return;
        profile.connection.disconnect(new TextComponentString(reason));
    }

    @Override
    public int getSlots() {
        return Minecraft.getMinecraft().getIntegratedServer().getPlayerList().maxPlayers;
    }

    @Override
    public void setSlots(int slots) {
        Minecraft.getMinecraft().getIntegratedServer().getPlayerList().maxPlayers = slots;
    }

    @Override
    public void stopServer() {
        Minecraft.getMinecraft().getIntegratedServer().getServer().getNetworkSystem().terminateEndpoints();
        Minecraft.getMinecraft().getIntegratedServer().isPublic = false;
    }
}
