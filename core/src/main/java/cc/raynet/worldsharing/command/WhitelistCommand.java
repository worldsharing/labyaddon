package cc.raynet.worldsharing.command;

import cc.raynet.worldsharing.WorldsharingAddon;
import io.sentry.Sentry;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;

public class WhitelistCommand extends Command {

    private final WorldsharingAddon addon;
    private final Component usage;

    public WhitelistCommand(WorldsharingAddon addon) {
        super("whitelist");
        this.addon = addon;
        translationKey("worldsharing.commands.whitelist");
        this.usage = Component.translatable("worldsharing.commands.usage", NamedTextColor.RED, Component.translatable(getTranslationKey() + ".usage", NamedTextColor.YELLOW, Component.text(prefix)));
    }

    @Override
    public boolean execute(String prefix, String[] args) {
        if (!addon.hasAccess() || !addon.sessionHandler.isConnected()) {
            return false;
        }

        if (args.length < 1 || ((args.length < 2) && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")))) {
            displayMessage(usage);
            return true;
        }

        try {
            switch (args[0]) {
                case "add" -> {
                    if (addon.sessionHandler.whitelistedPlayers.contains(args[1].toLowerCase())) {
                        displayMessage(Component.translatable(getTranslationKey() + ".already_whitelisted", NamedTextColor.RED, Component.text(args[1], NamedTextColor.YELLOW)));
                        return true;
                    }
                    addon.sessionHandler.manageWhitelist(args[1], true);
                    displayMessage(Component.translatable(getTranslationKey() + ".added", NamedTextColor.GRAY, Component.text(args[1], NamedTextColor.YELLOW)));
                }
                case "remove" -> {
                    if (!addon.sessionHandler.whitelistedPlayers.contains(args[1].toLowerCase())) {
                        displayMessage(Component.translatable(getTranslationKey() + ".not_whitelisted", NamedTextColor.RED, Component.text(args[1], NamedTextColor.YELLOW)));
                        return true;
                    }
                    addon.sessionHandler.manageWhitelist(args[1], false);
                    displayMessage(Component.translatable(getTranslationKey() + ".removed", NamedTextColor.GRAY, Component.text(args[1], NamedTextColor.YELLOW)));
                }
                case "list" ->
                        displayMessage(Component.translatable(getTranslationKey() + ".list", Component.text(String.join("\n", addon.sessionHandler.whitelistedPlayers))));
                default -> displayMessage(usage);
            }
        } catch (Exception e) {
            Sentry.captureException(e);
            addon.logger().error("Failed to manage whitelist", e);
        }

        return true;
    }
}
