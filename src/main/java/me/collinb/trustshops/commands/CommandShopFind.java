package me.collinb.trustshops.commands;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.shop.Shop;
import me.collinb.trustshops.shop.ShopTransactionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        List<Shop> shops;
        boolean pageArgumentIncluded = false;
        int page = 1;

        switch (args[0].toLowerCase()) {
            case "buying": {
                if (args.length >= 3) {
                    page = Integer.parseInt(args[2]);
                            pageArgumentIncluded = true;
                }

                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                if (item == null) {
                    commandSender.sendMessage("Invalid item: " + args[1]);
                    plugin.getChatManager().fail(commandSender, "Invalid item: " + args[1]);
                    return true;
                }
                shops = plugin.getDatabaseManager().findShopsByItem(item, ShopTransactionType.BUY);
                break;
            }
            case "selling": {
                if (args.length >= 3) {
                    page = Integer.parseInt(args[2]);
                    pageArgumentIncluded = true;
                }

                Material item = Material.getMaterial(args[1].toUpperCase().replace("MINECRAFT:", ""));
                if (item == null) {
                    plugin.getChatManager().fail(commandSender, "Invalid item: " + args[1]);
                    return false;
                }
                shops = plugin.getDatabaseManager().findShopsByItem(item, ShopTransactionType.SELL);
                break;
            }
            case "player": {
                if (args.length >= 3) {
                    page = Integer.parseInt(args[2]);
                    pageArgumentIncluded = true;
                }
                OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(args[1]);
                if (!shopOwner.hasPlayedBefore()) {
                    plugin.getChatManager().fail(commandSender, "Invalid player: " + args[1]);
                    return true;
                }
                shops = plugin.getDatabaseManager().findShopsByPlayer(shopOwner);
                break;
            }
            case "location": {
                if (args.length >= 5) {
                    page = Integer.parseInt(args[4]);
                    pageArgumentIncluded = true;
                }
                if (!(commandSender instanceof Player player)) {
                    plugin.getChatManager().fail(commandSender, "Finding shop by location can only be used by in-game players.");
                    return true;
                }
                if (args.length < 4) {
                    return false;
                }

                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                int z = Integer.parseInt(args[3]);

                Location location = new Location(player.getWorld(), x, y, z);
                shops = plugin.getDatabaseManager().findShopsByLocation(location);
                break;
            }
            default: {
                return false;
            }
        }
        List<@NotNull String> argsList = new ArrayList<>(List.of(args));
        if (pageArgumentIncluded) {
            argsList.remove(argsList.size() - 1);
        }
        String commandString = String.format("/%s %s", command.getLabel(), String.join(" ", String.join(" ", argsList)));
        plugin.getChatManager().sendShops(shops, commandSender, commandString, page);

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
                return plugin.getChatManager().getTabCompleteItems(strings, ((Player) commandSender).getWorld());
            } else if (strings[0].equals("location")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }

        return new ArrayList<>();
    }
}
