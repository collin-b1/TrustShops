package me.collinb.trustshops.listeners;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.ContainerShopPendingAction;
import me.collinb.trustshops.TrustShops;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Queue;

public class PlayerListener implements Listener {
    private final TrustShops plugin;
    private boolean destroyUnregistersShop;

    public PlayerListener(TrustShops plugin) {
        this.plugin = plugin;
        loadConfigVariables();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        handleShopAction(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (handleShopAction(event.getPlayer(), event.getClickedBlock())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (destroyUnregistersShop) {
            Block destroyedBlock = event.getBlock();
            if (TrustShops.ALLOWED_CONTAINERS.contains(destroyedBlock.getType())) {
                Location location = destroyedBlock.getLocation();
                Queue<ContainerShop> shops = plugin.getShopManager().getShopsByLocation(location);
                for (ContainerShop shop : shops) {
                    plugin.getShopManager().deleteShop(shop.getShopLocation());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getShopManager().popPendingInteraction(event.getPlayer());
    }

    /**
     * Handle shop actions from listeners
     *
     * @param player Player interacting with the shop
     * @param block  Shop block being interacted with
     * @return If the event should be canceled
     */
    public boolean handleShopAction(@NotNull Player player, @NotNull Block block) {
        ContainerShopPendingAction action = plugin.getShopManager().popPendingInteraction(player);
        if (action != null) {
            if (TrustShops.ALLOWED_CONTAINERS.contains(block.getType())) {
                switch (action.type()) {
                    case CREATE -> {
                        ContainerShop shop = action.shop();
                        shop.setShopLocation(block.getLocation());
                        if (plugin.getShopManager().registerShop(shop)) {
                            plugin.getChatManager().success(player, "Registered shop!");
                        } else {
                            plugin.getChatManager().fail(player, "Failed to register shop!");
                        }
                    }
                    case DELETE -> {
                        Queue<ContainerShop> shops = plugin.getShopManager().getShopsByLocation(block.getLocation());
                        if (shops.isEmpty()) {
                            plugin.getChatManager().fail(player, "Not a valid shop!");
                            return true;
                        }

                        for (ContainerShop shop : shops) {
                            if (!Objects.equals(shop.getShopOwner().getPlayer(), player)) {
                                if (!player.hasPermission("trustshops.tsdelete.others")) {
                                    plugin.getChatManager().fail(player, "You can't delete someone else's shop!");
                                    return true;
                                }
                            }

                            if (plugin.getShopManager().deleteShop(block.getLocation())) {
                                plugin.getChatManager().success(player, "Deleted shop!");
                            } else {
                                plugin.getChatManager().fail(player, "Failed to delete shop!");
                            }
                        }
                    }
                    case INFO -> {
                        Queue<ContainerShop> shops = plugin.getShopManager().getShopsByLocation(block.getLocation());
                        if (!shops.isEmpty()) {
                            plugin.getChatManager().sendShops(shops, player, 1);
                        } else {
                            plugin.getChatManager().fail(player, "Not a valid shop!");
                        }
                    }
                    default -> plugin.getChatManager().fail(player, "Invalid shop operation!");
                }
                return true;
            } else {
                plugin.getChatManager().fail(player, "Invalid container type!");
            }
        }
        return false;
    }

    public void loadConfigVariables() {
        this.destroyUnregistersShop = plugin.getConfig().getBoolean("delete-shop-on-destroy", true);
    }
}
