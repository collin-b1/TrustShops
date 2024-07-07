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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandShops implements CommandExecutor, TabCompleter {
    private static final String[] SUBCOMMANDS = {"buying", "selling", "player", "info"};
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

        Queue<ContainerShop> shopQueue;
        switch (args[0].toLowerCase()) {
            case "buying": {
                if (args.length < 2) return false;
                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                shopQueue = shopManager.findShopsByItem(item, ContainerShopTransactionType.BUY);
                if (item == null) {
                    commandSender.sendMessage(Component.text("Invalid item!").color(NamedTextColor.RED));
                    return false;
                }
                break;
            }
            case "selling": {
                if (args.length < 2) return false;
                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                shopQueue = shopManager.findShopsByItem(item, ContainerShopTransactionType.SELL);
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
                shopQueue = shopManager.findShopsByPlayer(shopOwner);
                break;
            }
            case "info": {
                if (!(commandSender instanceof Player player)) {
                    commandSender.sendMessage(Component.text("This command can only be used in-game.").color(NamedTextColor.RED));
                    return true;
                }

                if (shopManager.awaitInteraction(player, null, ContainerShopModificationType.INFO)) {
                    commandSender.sendMessage(Component.text("Interact with a shop to view its info.").color(NamedTextColor.YELLOW));
                } else {
                    commandSender.sendMessage(Component.text("Command failed: Already pending shop action!").color(NamedTextColor.RED));
                }
                return true;
            }
            default: {
                return false;
            }
        }
        chatManager.sendShops(shopQueue, commandSender);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            final List<String> completions = new ArrayList<>();

            StringUtil.copyPartialMatches(strings[0], List.of(SUBCOMMANDS), completions);

            return completions;
        } else if (strings.length == 2) {
            if (strings[0].equals("buying") || strings[0].equals("selling")) {
                String partialItem = strings[1].toUpperCase().replace("MINECRAFT:", "");
                return Arrays.stream(Material.values())
                        .map(material -> "minecraft:" + material.toString().toLowerCase())
                        .filter(name -> name.startsWith("minecraft:" + partialItem.toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            } else {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }

        return new ArrayList<>();
    }
}
