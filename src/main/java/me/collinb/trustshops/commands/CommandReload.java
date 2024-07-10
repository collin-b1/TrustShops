package me.collinb.trustshops.commands;

import me.collinb.trustshops.TrustShops;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.Bukkit.permissionMessage;

public class CommandReload implements CommandExecutor {
    private final TrustShops plugin;

    public CommandReload(TrustShops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("trustshops.tsreload")) {
            commandSender.sendMessage(permissionMessage());
            return true;
        }
        plugin.reload();
        plugin.getChatManager().success(commandSender, "Reloaded config!");
        return true;
    }
}
