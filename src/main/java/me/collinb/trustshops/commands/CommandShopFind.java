package me.collinb.trustshops.commands;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.enums.ContainerShopTransactionType;
import me.collinb.trustshops.utils.TabHelper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CommandShopFind implements CommandExecutor, TabCompleter {
    private static final String[] SUBCOMMANDS = {"buying", "selling", "player"};
    private final TrustShops plugin;

    public CommandShopFind(TrustShops plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 2) {
            return false;
        }

        int page = args.length < 3 ? 1 : Integer.parseInt(args[2]);

        Queue<ContainerShop> shopQueue;
        switch (args[0].toLowerCase()) {
            case "buying": {
                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                if (item == null) {
                    commandSender.sendMessage("Invalid item: " + args[1]);
                    plugin.getChatManager().fail(commandSender, "Invalid item: " + args[1]);
                    return true;
                }
                shopQueue = plugin.getShopManager().findShopsByItem(item, ContainerShopTransactionType.BUY);
                break;
            }
            case "selling": {
                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                if (item == null) {
                    plugin.getChatManager().fail(commandSender, "Invalid item: " + args[1]);
                    return false;
                }
                shopQueue = plugin.getShopManager().findShopsByItem(item, ContainerShopTransactionType.SELL);
                break;
            }
            case "player": {
                OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(args[1]);
                if (!shopOwner.hasPlayedBefore()) {
                    plugin.getChatManager().fail(commandSender, "Invalid player: " + args[1]);
                    return true;
                }
                shopQueue = plugin.getShopManager().findShopsByPlayer(shopOwner);
                break;
            }
            default: {
                return false;
            }
        }
        plugin.getChatManager().sendShops(shopQueue, commandSender, page);

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
                return TabHelper.getTabCompleteItems(strings, ((Player) commandSender).getWorld());
            } else {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }

        return new ArrayList<>();
    }
}
