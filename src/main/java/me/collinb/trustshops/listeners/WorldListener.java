package me.collinb.trustshops.listeners;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.managers.ContainerShopManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;

public class WorldListener implements Listener {
    private final ContainerShopManager shopManager;

    public WorldListener(ContainerShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        if (TrustShops.ALLOWED_CONTAINERS.contains(event.getBlock().getType())) {
            Location location = event.getBlock().getLocation();
            ContainerShop shop = shopManager.getShopByLocation(location);
            if (shop != null) {
                shopManager.deleteShop(location);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (TrustShops.ALLOWED_CONTAINERS.contains(event.getBlock().getType())) {
            Location location = event.getBlock().getLocation();
            ContainerShop shop = shopManager.getShopByLocation(location);
            if (shop != null) {
                shopManager.deleteShop(location);
            }
        }
    }
}
