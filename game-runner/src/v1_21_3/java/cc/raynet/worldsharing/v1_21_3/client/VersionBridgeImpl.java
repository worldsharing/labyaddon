package cc.raynet.worldsharing.v1_21_3.client;

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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

public class VersionBridgeImpl implements VersionBridge {

    @Override
    public void openChannel(ChannelHandler client) {
        new Bootstrap().group(ServerConnectionListener.SERVER_EVENT_GROUP.get()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast("handler", client);
            }
        }).channel(LocalChannel.class).connect(VersionStorage.proxyChannelAddress).syncUninterruptibly();
    }

    @Override
    public int getSuitableLanPort() {
        return HttpUtil.getAvailablePort();
    }

    @Override
    public boolean publishLanWorld(int port, GameMode gamemode, boolean allowCheats) {
        return Minecraft.getInstance()
                .getSingleplayerServer()
                .publishServer(GameType.byId(gamemode.getId()), allowCheats, port);
    }

    @Override
    public String getWorldName() {
        return Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName();
    }

    @Override
    public boolean isPublished() {
        return Minecraft.getInstance().getSingleplayerServer().isPublished();
    }

    @Override
    public void changeGameMode(GameMode gameMode) {
        if (!isPublished()) {
            return;
        }
        Minecraft.getInstance().getSingleplayerServer().setDefaultGameType(GameType.byId(gameMode.getId()));
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.fromId(Minecraft.getInstance().getSingleplayerServer().getDefaultGameType().getId());
    }

    @Override
    public void changeDifficulty(GameDifficulty difficulty) {
        Minecraft.getInstance().getSingleplayerServer().setDifficulty(Difficulty.byId(difficulty.getId()), true);
    }

    @Override
    public GameDifficulty getDifficulty() {
        return GameDifficulty.fromId(Minecraft.getInstance()
                .getSingleplayerServer()
                .getWorldData()
                .getDifficulty()
                .getId());
    }

    @Override
    public boolean cheatsEnabled() {
        return Minecraft.getInstance().getSingleplayerServer().getPlayerList().isAllowCommandsForAllPlayers();
    }

    @Override
    public void setCheatsEnabled(boolean enabled) {
        PlayerList playerList = Minecraft.getInstance().getSingleplayerServer().getPlayerList();
        playerList.setAllowCommandsForAllPlayers(enabled);
        for (var pl : playerList.getPlayers()) {
            playerList.sendPlayerPermissionLevel(pl);
        }
    }

    @Override
    public void kickPlayer(String name, String reason) {
        ServerPlayer profile = Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayerByName(name);
        if (profile == null) return;
        profile.connection.disconnect(Component.literal(reason));
    }

    @Override
    public int getSlots() {
        return Minecraft.getInstance().getSingleplayerServer().getPlayerList().maxPlayers;
    }

    @Override
    public void setSlots(int slots) {
        Minecraft.getInstance().getSingleplayerServer().getPlayerList().maxPlayers = slots;
    }

    @Override
    public void stopServer() {
        Minecraft.getInstance().getSingleplayerServer().getConnection().stop();
        Minecraft.getInstance().getSingleplayerServer().publishedPort = -1;
    }
}
