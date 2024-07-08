package me.collinb.trustshops.commands;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.enums.ContainerShopModificationType;
import me.collinb.trustshops.utils.ItemHelper;
import me.collinb.trustshops.utils.TabHelper;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandShopCreate implements CommandExecutor, TabCompleter {
    private final TrustShops plugin;

    public CommandShopCreate(TrustShops plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getChatManager().fail(sender, "This command may only be used in-game.");
            return true;
        }

        if (args.length < 4) {
            return false;
        }

        // Sold item
        String soldItemName = args[0].toUpperCase().replace("MINECRAFT:", "");
        Material soldItem = Material.getMaterial(soldItemName);
        int soldAmount = Integer.parseInt(args[1]);

        if (!ItemHelper.isValidShopItem(soldItem)) {
            plugin.getChatManager().fail(sender, "Invalid item: " + args[0]);
            return true;
        }

        // Bought item
        String boughtItemName = args[2].toUpperCase().replace("MINECRAFT:", "");
        Material boughtItem = Material.getMaterial(boughtItemName);
        int boughtAmount = Integer.parseInt(args[3]);

        if (!ItemHelper.isValidShopItem(boughtItem)) {
            plugin.getChatManager().fail(sender, "Invalid item: " + args[2]);
            return true;
        }

        ContainerShop shop = new ContainerShop(player, soldItem, soldAmount, boughtItem, boughtAmount);

        if (plugin.getShopManager().awaitInteraction(player, shop, ContainerShopModificationType.CREATE)) {
            plugin.getChatManager().warning(player, "Interact with a valid container to register shop.");
        } else {
            plugin.getChatManager().fail(player, "Creation failed: Already pending shop action!");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1 || strings.length == 3) {
            return TabHelper.getTabCompleteItems(strings, ((Player) commandSender).getWorld());
        }

        return new ArrayList<>();
    }
}
