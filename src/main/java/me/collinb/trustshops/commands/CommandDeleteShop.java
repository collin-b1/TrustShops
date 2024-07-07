package me.collinb.trustshops.commands;

import me.collinb.trustshops.enums.ContainerShopModificationType;
import me.collinb.trustshops.managers.ContainerShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandDeleteShop implements CommandExecutor {
    private final ContainerShopManager shopManager;

    public CommandDeleteShop(ContainerShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be used by in-game players.");
            return true;
        }

        if (shopManager.awaitInteraction(player, null, ContainerShopModificationType.DELETE)) {
            player.sendMessage(Component.text("Interact with a valid shop to delete.").color(NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("Deletion failed: Already pending shop action!").color(NamedTextColor.RED));
        }

        return true;
    }
}
