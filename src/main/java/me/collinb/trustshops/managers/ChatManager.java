package me.collinb.trustshops.managers;

import me.collinb.trustshops.ContainerShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class ChatManager {
    public TextComponent getDisplayHeader() {
        return Component.text("=====<").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("TrustShops").color(NamedTextColor.AQUA))
                .append(Component.text(">=====").color(NamedTextColor.DARK_GRAY));
    }

    public TextComponent getDisplayFooter() {
        return Component.text("=====================").color(NamedTextColor.DARK_GRAY);
    }

    public void sendShopSet(Set<ContainerShop> shopSet, CommandSender player) {
        player.sendMessage(getDisplayHeader());
        for (ContainerShop shop : shopSet) {
            player.sendMessage(shop.getDisplayLine());
        }
        player.sendMessage(getDisplayFooter());
    }
}
