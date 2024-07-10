package me.collinb.trustshops.commands;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.shop.ShopModificationType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandShopDelete implements CommandExecutor {
    private final TrustShops plugin;

    public CommandShopDelete(TrustShops plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            plugin.getChatManager().fail(commandSender, "This command can only be used by in-game players.");
            return true;
        }

        if (plugin.getPluginConfig().awaitInteraction(player, null, ShopModificationType.DELETE)) {
            plugin.getChatManager().warning(player, "Interact with a valid shop to delete.");
        } else {
            plugin.getChatManager().fail(player, "Deletion failed: Already pending shop action!");
        }

        return true;
    }
}
