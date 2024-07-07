package me.collinb.trustshops.commands;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.enums.ContainerShopModificationType;
import me.collinb.trustshops.managers.ContainerShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandCreateShop implements CommandExecutor, TabCompleter {
    private final ContainerShopManager shopManager;
    public CommandCreateShop(ContainerShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by in-game players.");
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        // Bought item
        String boughtItemName = args[0].toUpperCase().replace("MINECRAFT:", "");
        Material boughtItem = Material.getMaterial(boughtItemName);
        if (boughtItem == null) {
            sender.sendMessage("Invalid item: " + args[0]);
            return true;
        }
        int boughtAmount = Integer.parseInt(args[1]);

        // Sold item
        String soldItemName;
        Material soldItem;
        int soldAmount;
        if (args.length < 4) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            soldItem = handItem.getType();
            soldAmount = handItem.getAmount();
        } else {
            soldItemName = args[2].toUpperCase().replace("MINECRAFT:", "");
            soldItem = Material.getMaterial(soldItemName);
            soldAmount = Integer.parseInt(args[3]);
        }
        if (soldItem == null) {
            sender.sendMessage("Invalid item: " + args[2]);
            return true;
        }

        ContainerShop shop = new ContainerShop(player, soldItem, soldAmount, boughtItem, boughtAmount);

        if (shopManager.awaitInteraction(player, shop, ContainerShopModificationType.CREATE)) {
            player.sendMessage(Component.text("Interact with a valid container to register shop.").color(NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("Creation failed: Already pending shop action!").color(NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1 || strings.length == 3) {
            String partialItem = strings[strings.length - 1].toUpperCase().replace("MINECRAFT:", "");
            return Arrays.stream(Material.values())
                    .map(material -> "minecraft:" + material.toString().toLowerCase())
                    .filter(name -> name.startsWith("minecraft:" + partialItem.toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
