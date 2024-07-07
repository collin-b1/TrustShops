package me.collinb.trustshops.listeners;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.ContainerShopPendingAction;
import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.managers.ContainerShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class PlayerListener implements Listener {
    private final ContainerShopManager shopManager;
    public PlayerListener(ContainerShopManager shopManager) {
        this.shopManager = shopManager;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (handleShopAction(event.getPlayer(), event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleShopAction(event.getPlayer(), event.getBlockPlaced());
    }

    /** Handle shop actions from listeners
     *
     * @param player Player interacting with the shop
     * @param block Shop block being interacted with
     * @return If the event should be canceled
     */
    public boolean handleShopAction(Player player, Block block) {
        ContainerShopPendingAction action = shopManager.popPendingInteraction(player);
        if (action != null) {
            if (TrustShops.ALLOWED_CONTAINERS.contains(Objects.requireNonNull(block).getType())) {
                switch (action.type()) {
                    case CREATE -> {
                        ContainerShop shop = action.shop();
                        shop.setShopLocation(block.getLocation());
                        if (shopManager.registerShop(shop)) {
                            player.sendMessage(Component.text("Registered shop!").color(NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("Failed to register shop!").color(NamedTextColor.RED));
                        }
                    }
                    case DELETE -> {
                        if (shopManager.deleteShop(block.getLocation())) {
                            player.sendMessage(Component.text("Deleted shop!").color(NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("Failed to delete shop!").color(NamedTextColor.GREEN));
                        }
                    }
                    case INFO -> {
                        ContainerShop shop = shopManager.getShopByLocation(block.getLocation());
                        if (shop != null) {
                            player.sendMessage(shop.getDisplayLine());
                        } else {
                            player.sendMessage(Component.text("Invalid shop!").color(NamedTextColor.RED));
                        }
                    }
                    default -> player.sendMessage(Component.text("Invalid shop operation!").color(NamedTextColor.RED));
                }
                return true;
            } else {
                player.sendMessage(Component.text("Invalid container type!").color(NamedTextColor.RED));
            }
        }
        return false;
    }
}
