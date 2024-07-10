package me.collinb.trustshops.config;

import com.google.common.collect.ImmutableSet;
import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.shop.Shop;
import me.collinb.trustshops.shop.ShopModificationType;
import me.collinb.trustshops.shop.ShopPendingAction;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Config {
    private final TrustShops plugin;

    private Map<Player, ShopPendingAction> awaitingInteraction;

    private Set<Material> allowedContainers;
    private Set<Material> bannedItems;
    private Set<World> bannedWorlds;
    private String tablePrefix;
    private int shopsPerPage;
    private boolean showOfflinePlayerShops;
    private boolean showUnstockedPlayerShops;
    private int minBuyingAmount;
    private int maxBuyingAmount;
    private int minSellingAmount;
    private int maxSellingAmount;
    private boolean deleteShopsOnDestroy;
    private boolean deleteShopsOnBan;

    public Config(TrustShops plugin) {
        this.plugin = plugin;
        plugin.getConfig();
        reloadConfig();
    }

    public Set<Material> getAllowedContainers() {
        return allowedContainers;
    }

    public Set<Material> getBannedItems() {
        return bannedItems;
    }

    public Set<World> getBannedWorlds() {
        return bannedWorlds;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public int getShopsPerPage() {
        return shopsPerPage;
    }

    public boolean showOfflinePlayerShops() {
        return showOfflinePlayerShops;
    }

    public boolean showUnstockedPlayerShops() {
        return showUnstockedPlayerShops;
    }

    public int getMinBuyingAmount() {
        return minBuyingAmount;
    }

    public int getMaxBuyingAmount() {
        return maxBuyingAmount;
    }

    public int getMinSellingAmount() {
        return minSellingAmount;
    }

    public int getMaxSellingAmount() {
        return maxSellingAmount;
    }

    public boolean deleteShopsOnDestroy() {
        return deleteShopsOnDestroy;
    }

    public boolean deleteShopsOnBan() {
        return deleteShopsOnBan;
    }

    public boolean awaitInteraction(Player player, Shop shop, ShopModificationType type) {
        if (awaitingInteraction.containsKey(player)) {
            return false;
        } else {
            awaitingInteraction.put(player, new ShopPendingAction(shop, type));
            return true;
        }
    }

    public ShopPendingAction popPendingInteraction(Player player) {
        return awaitingInteraction.remove(player);
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration pluginConfig = plugin.getConfig();

        awaitingInteraction = new HashMap<>();

        tablePrefix = pluginConfig.getString("data.table-prefix", "ts_");
        shopsPerPage = pluginConfig.getInt("max-shops-per-page", 10);
        showOfflinePlayerShops = pluginConfig.getBoolean("show-offline-player-shops", true);
        showUnstockedPlayerShops = pluginConfig.getBoolean("show-out-of-stock-shops", true);
        minBuyingAmount = pluginConfig.getInt("min-buying-amount", 1);
        maxBuyingAmount = pluginConfig.getInt("max-buying-amount", 3456);
        minSellingAmount = pluginConfig.getInt("min-selling-amount", 1);
        maxSellingAmount = pluginConfig.getInt("max-selling-amount", 3456);
        deleteShopsOnDestroy = pluginConfig.getBoolean("delete-shop-on-destroy", true);
        deleteShopsOnBan = pluginConfig.getBoolean("delete-shop-on-ban", false);

        // Load whitelisted containers
        if (pluginConfig.contains("allowed-containers")) {
            this.allowedContainers = pluginConfig.getStringList("allowed-containers")
                    .stream()
                    .map(Material::getMaterial)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toSet(),
                            ImmutableSet::copyOf
                    ));
        } else {
            allowedContainers = ImmutableSet.of(Material.CHEST);
        }

        // Load banned items
        if (pluginConfig.contains("banned-items")) {
            bannedItems = pluginConfig.getStringList("banned-items")
                    .stream()
                    .map(Material::getMaterial)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toSet(),
                            ImmutableSet::copyOf
                    ));
        } else {
            bannedItems = Collections.emptySet();
        }

        // Load banned worlds
        if (pluginConfig.contains("disabled-worlds")) {
            bannedWorlds = pluginConfig.getStringList("disabled-worlds")
                    .stream()
                    .map(worldName -> plugin.getServer().getWorld(worldName))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toSet(),
                            ImmutableSet::copyOf
                    ));
        } else {
            bannedWorlds = Collections.emptySet();
        }

        plugin.getLogger().info(String.format("Loaded %d containers", allowedContainers.size()));
        plugin.getLogger().info(String.format("Loaded %d banned items", bannedItems.size()));
        plugin.getLogger().info(String.format("Loaded %d disabled worlds", bannedWorlds.size()));
    }
}
