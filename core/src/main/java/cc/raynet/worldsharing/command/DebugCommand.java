package cc.raynet.worldsharing.command;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.client.chat.command.Command;

public class DebugCommand extends Command {

    private final WorldsharingAddon instance;

    public DebugCommand() {
        super("wsdebug");
        this.instance = WorldsharingAddon.INSTANCE;
    }


    @Override
    public boolean execute(String prefix, String[] arguments) {
        if (!instance.sessionHandler.isConnected()) {
            return false;
        }

        StringBuilder message = new StringBuilder();

        message.append("§cState: §f").append(instance.sessionHandler.getState()).append("\n")
                .append("§cConnected: §f").append(instance.sessionHandler.isConnected()).append("\n")
                .append("§cWhitelisted: §f").append(String.join(", ", instance.sessionHandler.whitelistedPlayers)).append("\n")
                .append("§cHostname: §f").append(instance.sessionHandler.tunnelInfo.hostname).append("\n")
                .append("§cVisibility: §f").append(instance.sessionHandler.tunnelInfo.visibility).append("\n");

        if (!instance.sessionHandler.players.isEmpty()) {
            message.append("§cConnected: §f\n");
            for (var p : instance.sessionHandler.players) {
                message.append("§c> ")
                        .append(p.username)
                        .append(p.isBedrock ? "§7 (bedrock) §c" : " ")
                        .append("§7- §c")
                        .append(parseNode(p.nodeIP))
                        .append("\n");
            }
        }

        displayMessage(message.toString());
        return true;
    }

    private String parseNode(String ip) {
        for (var n : instance.nodes.entrySet()) {
            if (n.getValue().getHostAddress().equals(ip)) {
                return n.getKey() + " (" + ip + ")";
            }
        }
        return ip;
    }
}
