package me.collinb.trustshops.commands;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.shop.Shop;
import me.collinb.trustshops.shop.ShopModificationType;
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

        int minSellAmount = plugin.getPluginConfig().getMinSellingAmount();
        int maxSellAmount = plugin.getPluginConfig().getMaxSellingAmount();

        int minBuyAmount = plugin.getPluginConfig().getMinBuyingAmount();
        int maxBuyAmount = plugin.getPluginConfig().getMaxBuyingAmount();

        if (!(sender instanceof Player player)) {
            plugin.getChatManager().fail(sender, "This command may only be used in-game.");
            return true;
        }

        if (args.length < 4) {
            return false;
        }

        if (plugin.getPluginConfig().getBannedWorlds().contains(player.getWorld())) {
            plugin.getChatManager().fail(sender, "You cannot create a shop in this world!");
            return true;
        }

        // Sold item
        String soldItemName = args[0].toUpperCase().replace("MINECRAFT:", "");
        Material soldItem = Material.getMaterial(soldItemName);
        int soldAmount;
        try {
            soldAmount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            plugin.getChatManager().fail(sender, "Invalid sell amount.");
            return true;
        }

        if (!plugin.isValidShopItem(soldItem)) {
            plugin.getChatManager().fail(sender, "Invalid item: " + args[0]);
            return true;
        }

        if (soldAmount < minSellAmount || soldAmount > maxSellAmount) {
            plugin.getChatManager().fail(sender, String.format("Invalid sell amount! Must be between [%d, %d]", minSellAmount, maxSellAmount));
            return true;
        }

        // Bought item
        String boughtItemName = args[2].toUpperCase().replace("MINECRAFT:", "");
        Material boughtItem = Material.getMaterial(boughtItemName);
        int boughtAmount;
        try {
            boughtAmount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            plugin.getChatManager().fail(sender, "Invalid buy amount.");
            return true;
        }

        if (!plugin.isValidShopItem(boughtItem)) {
            plugin.getChatManager().fail(sender, "Invalid item: " + args[2]);
            return true;
        }

        if (boughtAmount < minBuyAmount || boughtAmount > maxBuyAmount) {
            plugin.getChatManager().fail(sender, String.format("Invalid buy amount! Must be between [%d, %d]", minBuyAmount, maxBuyAmount));
            return true;
        }

        Shop shop = new Shop(player, soldItem, soldAmount, boughtItem, boughtAmount);

        if (plugin.getPluginConfig().awaitInteraction(player, shop, ShopModificationType.CREATE)) {
            plugin.getChatManager().warning(player, "Interact with a valid container to register shop.");
        } else {
            plugin.getChatManager().fail(player, "Creation failed: Already pending shop action!");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1 || strings.length == 3) {
            return plugin.getChatManager().getTabCompleteItems(strings, ((Player) commandSender).getWorld());
        }

        return new ArrayList<>();
    }
}
