package me.collinb.trustshops.listeners;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.config.Config;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WorldListener implements Listener {
    private final TrustShops plugin;
    private final Config config;

    public WorldListener(TrustShops plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
    }

    // Entity explosions (TNT, creeper, etc.)
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (config.deleteShopsOnDestroy()) {
            for (Block explodedBlock : event.blockList()) {
                if (config.getAllowedContainers().contains(explodedBlock.getType())) {
                    Location location = explodedBlock.getLocation();
                    plugin.getDatabaseManager().deleteShopsByLocation(location);
                }
            }
        }
    }

    // Block explosions (such as bed)
    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        if (config.deleteShopsOnDestroy()) {
            for (Block explodedBlock : event.blockList()) {
                if (config.getAllowedContainers().contains(explodedBlock.getType())) {
                    Location location = explodedBlock.getLocation();
                    plugin.getDatabaseManager().deleteShopsByLocation(location);
                }
            }
        }
    }
}
