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
            displayMessage("§cnot connected");
            return true;
        }
        displayMessage("§cState: §f" + instance.sessionHandler.getState());
        displayMessage("§cConnected: §f" + instance.sessionHandler.isConnected());
        displayMessage("§cWhitelisted: §f" + String.join(", ", instance.sessionHandler.whitelistedPlayers));
        displayMessage("§cHostname: §f" + instance.sessionHandler.tunnelInfo.hostname);
        displayMessage("§cVisibility: §f" + instance.sessionHandler.tunnelInfo.visibility);
        if (!instance.sessionHandler.players.isEmpty()) {
            displayMessage("§cConnected: §f");
            for (var p : instance.sessionHandler.players) {
                displayMessage("§c> " + p.username + (p.isBedrock ? "§7 (bedrock) §c" : " ") + "§7- §c" + parseNode(p.nodeIP));
            }
        }

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
