package me.collinb.trustshops.commands;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.shop.ShopModificationType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandShopInfo implements CommandExecutor {
    private final TrustShops plugin;

    public CommandShopInfo(TrustShops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be used by in-game players.");
            return true;
        }

        if (plugin.getPluginConfig().awaitInteraction(player, null, ShopModificationType.INFO)) {
            plugin.getChatManager().info(commandSender, "Interact with a valid container to view its shops");
        } else {
            plugin.getChatManager().fail(commandSender, "Info failed: Already pending shop action!");
        }

        return true;
    }
}
