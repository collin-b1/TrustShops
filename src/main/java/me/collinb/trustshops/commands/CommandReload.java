package me.collinb.trustshops.commands;

import me.collinb.trustshops.TrustShops;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandReload implements CommandExecutor {
    private final TrustShops plugin;

    public CommandReload(TrustShops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("trustshops.tsreload")) {
            plugin.getChatManager().fail(commandSender, "You do not have permission to reload the plugin.");
            return true;
        }
        plugin.reload();
        plugin.getChatManager().success(commandSender, "Reloaded config!");
        return true;
    }
}
