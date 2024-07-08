package me.collinb.trustshops.managers;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.TrustShops;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

public class ChatManager {
    private final TrustShops plugin;
    private int shopsPerPage;

    public ChatManager(TrustShops plugin) {
        this.plugin = plugin;
        loadConfigVariables();
    }

    public TextComponent getDisplayHeader() {
        return Component.text("=======<").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("TrustShops").color(NamedTextColor.AQUA))
                .append(Component.text(">=======").color(NamedTextColor.DARK_GRAY));
    }

    public TextComponent getDisplayFooter() {
        return Component.text("=========================").color(NamedTextColor.DARK_GRAY);
    }

    public void sendShops(Iterable<ContainerShop> shopSet, CommandSender player) {
        player.sendMessage(getDisplayHeader());
        Iterator<ContainerShop> iterator = shopSet.iterator();
        for (int i = 0; i < shopsPerPage && iterator.hasNext(); ++i) {
            ContainerShop shop = iterator.next();
            player.sendMessage(shop.getDisplayLine());
        }
        player.sendMessage(getDisplayFooter());
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

    public void loadConfigVariables() {
        this.shopsPerPage = plugin.getConfig().getInt("max-shops-per-page", 10);
    }
}
