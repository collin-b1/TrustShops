package me.collinb.trustshops;

import com.google.common.collect.ImmutableSet;
import me.collinb.trustshops.commands.*;
import me.collinb.trustshops.listeners.PlayerListener;
import me.collinb.trustshops.listeners.WorldListener;
import me.collinb.trustshops.managers.ChatManager;
import me.collinb.trustshops.managers.ContainerShopManager;
import me.collinb.trustshops.managers.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.stream.Collectors;


public final class TrustShops extends JavaPlugin {

    DatabaseManager databaseManager;
    ContainerShopManager shopManager;
    ChatManager chatManager;
    public static ImmutableSet<Material> ALLOWED_CONTAINERS;
    public static ImmutableSet<Material> BANNED_ITEMS;

    @Override
    public void onEnable() {
        reload();

        databaseManager = new DatabaseManager();
        shopManager = new ContainerShopManager(this);
        chatManager = new ChatManager(this);

        // Register events
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);

        // Register commands
        CommandShopCreate commandCreateShop = new CommandShopCreate(this);
        Objects.requireNonNull(this.getCommand("tscreate")).setExecutor(commandCreateShop);
        Objects.requireNonNull(this.getCommand("tscreate")).setTabCompleter(commandCreateShop);

        Objects.requireNonNull(this.getCommand("tsdelete")).setExecutor(new CommandShopDelete(this));

        Objects.requireNonNull(this.getCommand("tsinfo")).setExecutor(new CommandShopInfo(this));

        CommandShopFind commandFindShops = new CommandShopFind(this);
        Objects.requireNonNull(this.getCommand("tsfind")).setExecutor(commandFindShops);
        Objects.requireNonNull(this.getCommand("tsfind")).setTabCompleter(commandFindShops);

        Objects.requireNonNull(this.getCommand("tsreload")).setExecutor(new CommandReload(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {
        this.saveDefaultConfig();
        this.reloadConfig();

        // Load whitelisted containers
        if (this.getConfig().contains("allowed-containers")) {
            ALLOWED_CONTAINERS = this.getConfig().getStringList("allowed-containers")
                    .stream()
                    .map(Material::getMaterial)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toSet(),
                            ImmutableSet::copyOf
                    ));
        } else {
            ALLOWED_CONTAINERS = ImmutableSet.of(Material.CHEST);
        }

        // Load banned items
        if (this.getConfig().contains("banned-items")) {
            BANNED_ITEMS = this.getConfig().getStringList("banned-items")
                    .stream()
                    .map(Material::getMaterial)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toSet(),
                            ImmutableSet::copyOf
                    ));
        } else {
            BANNED_ITEMS = ImmutableSet.of();
        }

        getLogger().info(String.format("Loaded %d containers", ALLOWED_CONTAINERS.size()));
        getLogger().info(String.format("Loaded %d banned items", ALLOWED_CONTAINERS.size()));
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public ContainerShopManager getShopManager() {
        return shopManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
