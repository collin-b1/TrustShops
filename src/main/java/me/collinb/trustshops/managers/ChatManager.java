package me.collinb.trustshops.managers;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChatManager {
    private final TrustShops plugin;

    public ChatManager(TrustShops plugin) {
        this.plugin = plugin;
    }

    public TextComponent getDisplayHeader() {
        return Component.text("----- ").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("TrustShops").color(NamedTextColor.AQUA))
                .append(Component.text(" -----").color(NamedTextColor.DARK_GRAY));
    }

    public TextComponent getDisplayFooter(String commandString, int page, int totalPages) {
        String prevPageCommand = String.format("%s %d", commandString, page - 1);
        String nextPageCommand = String.format("%s %d", commandString, page + 1);

        TextComponent component = Component.text("--- ").color(NamedTextColor.DARK_GRAY);

        if (page > 1) {
            component = component.append(Component.text("◀ ")
                    .color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)
                    .hoverEvent(HoverEvent.showText(Component.text(prevPageCommand)))
                    .clickEvent(ClickEvent.runCommand(prevPageCommand))
            );
        }

        component = component
                .append(Component.text(String.format("Page %d of %d", page, totalPages))
                .color(NamedTextColor.GRAY)
        );

        if (page < totalPages) {
            component = component.append(Component.text(" ▶")
                    .color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)
                    .hoverEvent(HoverEvent.showText(Component.text(nextPageCommand)))
                    .clickEvent(ClickEvent.runCommand(nextPageCommand))
            );
        }
        return component;
    }

    public void sendShops(List<Shop> shops, CommandSender player, String commandString, int page) {
        int shopsPerPage = plugin.getPluginConfig().getShopsPerPage();
        int startingIndex = (page - 1) * shopsPerPage;

        player.sendMessage(getDisplayHeader());
        Collections.sort(shops);

        int totalPages = (int) Math.ceil((double) shops.size() / shopsPerPage);
        boolean showStock = player.hasPermission("trustshops.seeshopstock");
        boolean showLocation = player.hasPermission("trustshops.seeshoplocation");

        if (startingIndex >= 0 && startingIndex < shops.size()) {
            for (int i = startingIndex; i < (startingIndex + shopsPerPage) && i < shops.size(); ++i) {
                Shop shop = shops.get(i);
                player.sendMessage(shop.getDisplayLine(showStock, showLocation));
            }
        }

        player.sendMessage(getDisplayFooter(commandString, page, totalPages));
    }

    public Component getLabel() {
        return Component.text("[TrustShops] ").color(NamedTextColor.AQUA);
    }

    public void info(CommandSender player, String message) {
        player.sendMessage(getLabel().append(Component.text(message).color(NamedTextColor.GRAY)));
    }

    public void warning(CommandSender player, String message) {
        player.sendMessage(getLabel().append(Component.text(message).color(NamedTextColor.YELLOW)));
    }

    public void success(CommandSender player, String message) {
        player.sendMessage(getLabel().append(Component.text(message).color(NamedTextColor.GREEN)));
    }

    public void fail(CommandSender player, String message) {
        player.sendMessage(getLabel().append(Component.text(message).color(NamedTextColor.RED)));
    }

    public List<String> getTabCompleteItems(@NotNull String[] strings, @NotNull World world) {
        String partialItem = strings[strings.length - 1].toUpperCase().replace("MINECRAFT:", "");
        return Arrays.stream(Material.values())
                .filter(material -> material.isEnabledByFeature(world))
                .filter(plugin::isValidShopItem)
                .map(material -> "minecraft:" + material.toString().toLowerCase())
                .filter(name -> name.startsWith("minecraft:" + partialItem.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}
