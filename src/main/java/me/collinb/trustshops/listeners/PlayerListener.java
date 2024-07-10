package me.collinb.trustshops.listeners;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.config.Config;
import me.collinb.trustshops.shop.Shop;
import me.collinb.trustshops.shop.ShopPendingAction;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PlayerListener implements Listener {
    private final TrustShops plugin;
    private final Config config;

    public PlayerListener(TrustShops plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
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
        Player player = event.getPlayer();
        boolean destroyUnregistersShop = config.deleteShopsOnDestroy();
        if (destroyUnregistersShop) {
            Block destroyedBlock = event.getBlock();
            if (config.getAllowedContainers().contains(destroyedBlock.getType())) {
                Location location = destroyedBlock.getLocation();
                List<Shop> shops = plugin.getDatabaseManager().findShopsByLocation(location);
                for (Shop shop : shops) {
                    if (shop.getShopOwner().equals(player)) {
                        plugin.getDatabaseManager().deleteShop(shop.getShopLocation());
                    } else {
                        Player shopOwner = shop.getShopOwner().getPlayer();
                        if (shopOwner != null) {
                            plugin.getChatManager().warning(shopOwner, String.format("Your shop at %s was destroyed by %s!", shop.getShopLocation().toString(), player.getName()));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        config.popPendingInteraction(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (config.deleteShopsOnBan()) {
            if (event.getCause() == PlayerKickEvent.Cause.BANNED || event.getCause() == PlayerKickEvent.Cause.IP_BANNED) {
                OfflinePlayer bannedPlayer = event.getPlayer();
                List<Shop> shops = plugin.getDatabaseManager().findShopsByPlayer(bannedPlayer);
                for (Shop shop : shops) {
                    plugin.getDatabaseManager().deleteShop(shop.getShopLocation());
                }
            }
        }
    }

    /**
     * Handle shop actions from listeners
     *
     * @param player Player interacting with the shop
     * @param block  Shop block being interacted with
     * @return If the event should be canceled
     */
    public boolean handleShopAction(@NotNull Player player, @NotNull Block block) {
        ShopPendingAction action = config.popPendingInteraction(player);
        if (action != null) {
            if (config.getAllowedContainers().contains(block.getType())) {
                switch (action.type()) {
                    case CREATE -> {
                        Shop shop = action.shop();
                        shop.setShopLocation(block.getLocation());
                        if (plugin.getDatabaseManager().registerShop(shop)) {
                            plugin.getChatManager().success(player, "Registered shop!");
                        } else {
                            plugin.getChatManager().fail(player, "Failed to register shop!");
                        }
                    }
                    case DELETE -> {
                        List<Shop> shops = plugin.getDatabaseManager().findShopsByLocation(block.getLocation());
                        if (shops.isEmpty()) {
                            plugin.getChatManager().fail(player, "Not a valid shop!");
                            return true;
                        }

                        for (Shop shop : shops) {
                            if (!Objects.equals(shop.getShopOwner().getPlayer(), player)) {
                                if (!player.hasPermission("trustshops.tsdelete.others")) {
                                    plugin.getChatManager().fail(player, "You can't delete someone else's shop!");
                                    return true;
                                }
                            }

                            if (plugin.getDatabaseManager().deleteShop(block.getLocation())) {
                                plugin.getChatManager().success(player, "Deleted shop!");
                            } else {
                                plugin.getChatManager().fail(player, "Failed to delete shop!");
                            }
                        }
                    }
                    case INFO -> {
                        List<Shop> shops = plugin.getDatabaseManager().findShopsByLocation(block.getLocation());
                        if (!shops.isEmpty()) {
                            String commandString = String.format("/%s location %d %d %d", plugin.getCommand("tsfind").getLabel(), block.getX(), block.getY(), block.getZ());
                            plugin.getChatManager().sendShops(shops, player, commandString, 1);
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
}
