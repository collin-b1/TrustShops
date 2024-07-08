package me.collinb.trustshops.listeners;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.TrustShops;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Queue;

public class WorldListener implements Listener {
    private final TrustShops plugin;

    public WorldListener(TrustShops plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        for (Block explodedBlock : event.blockList()) {
            if (TrustShops.ALLOWED_CONTAINERS.contains(explodedBlock.getType())) {
                Location location = explodedBlock.getLocation();
                Queue<ContainerShop> shops = plugin.getShopManager().getShopsByLocation(location);
                for (ContainerShop shop : shops) {
                    plugin.getShopManager().deleteShop(shop.getShopLocation());
                }
            }
        }
    }
}
