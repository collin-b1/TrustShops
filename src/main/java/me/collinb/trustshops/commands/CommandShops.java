package me.collinb.trustshops.commands;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.enums.ContainerShopModificationType;
import me.collinb.trustshops.enums.ContainerShopTransactionType;
import me.collinb.trustshops.managers.ChatManager;
import me.collinb.trustshops.managers.ContainerShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class CommandShops implements CommandExecutor {
    private final ContainerShopManager shopManager;
    private final ChatManager chatManager;
    public CommandShops(ContainerShopManager shopManager, ChatManager chatManager) {
        this.shopManager = shopManager;
        this.chatManager = chatManager;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }

        Set<ContainerShop> shopSet;
        switch (args[0].toLowerCase()) {
            case "buying": {
                if (args.length < 2) return false;
                shopSet = shopManager.findShopsByItem(Objects.requireNonNull(Material.getMaterial(args[1])), ContainerShopTransactionType.BUY);
                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                if (item == null) {
                    commandSender.sendMessage(Component.text("Invalid item!").color(NamedTextColor.RED));
                    return false;
                }
                break;
            }
            case "selling": {
                if (args.length < 2) return false;
                shopSet = shopManager.findShopsByItem(Objects.requireNonNull(Material.getMaterial(args[1])), ContainerShopTransactionType.SELL);
                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                if (item == null) {
                    commandSender.sendMessage(Component.text("Invalid item!").color(NamedTextColor.RED));
                    return false;
                }
                break;
            }
            case "player": {
                if (args.length < 2) return false;
                OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(args[1]);
                if (!shopOwner.hasPlayedBefore()) {
                    commandSender.sendMessage(Component.text("Invalid player!").color(NamedTextColor.RED));
                    return true;
                }
                shopSet = shopManager.findShopsByPlayer(shopOwner);
                break;
            }
            case "info": {
                if (!(commandSender instanceof Player player)) {
                    commandSender.sendMessage(Component.text("This command can only be used in-game.").color(NamedTextColor.RED));
                    return true;
                }
                commandSender.sendMessage(Component.text("Interact with a shop to view its info.").color(NamedTextColor.YELLOW));
                shopManager.awaitInteraction(player, null, ContainerShopModificationType.INFO);
                return true;
            }
            default: {
                return false;
            }
        }
        chatManager.sendShopSet(shopSet, commandSender);

        return true;
    }
}
