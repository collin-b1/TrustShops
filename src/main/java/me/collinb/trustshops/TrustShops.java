package me.collinb.trustshops;

import me.collinb.trustshops.commands.*;
import me.collinb.trustshops.config.Config;
import me.collinb.trustshops.listeners.PlayerListener;
import me.collinb.trustshops.listeners.WorldListener;
import me.collinb.trustshops.managers.ChatManager;
import me.collinb.trustshops.managers.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;


public final class TrustShops extends JavaPlugin {

    private Config config;
    private DatabaseManager databaseManager;
    private ChatManager chatManager;

    @Override
    public void onEnable() {
        this.config = new Config(this);

        this.databaseManager = new DatabaseManager(this);
        this.chatManager = new ChatManager(this);

        // Register events
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);

        // Register commands
        CommandShopCreate commandCreateShop = new CommandShopCreate(this);
        this.getCommand("tscreate").setExecutor(commandCreateShop);
        this.getCommand("tscreate").setTabCompleter(commandCreateShop);

        this.getCommand("tsdelete").setExecutor(new CommandShopDelete(this));

        this.getCommand("tsinfo").setExecutor(new CommandShopInfo(this));

        CommandShopFind commandFindShops = new CommandShopFind(this);
        this.getCommand("tsfind").setExecutor(commandFindShops);
        this.getCommand("tsfind").setTabCompleter(commandFindShops);

        this.getCommand("tsreload").setExecutor(new CommandReload(this));
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    /**
     * Reload plugin config values, close database connection, reassign managers.
     */
    public void reload() {
        config.reloadConfig();

        databaseManager.closeConnection();

        databaseManager = new DatabaseManager(this);
        chatManager = new ChatManager(this);
    }

    public Config getPluginConfig() {
        return this.config;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Check if a material can be used in a shop
     * @param material Material to check
     * @return If the material can be used in a shop
     */
    public boolean isValidShopItem(Material material) {
        return material != null
                && material.isItem()
                && !getPluginConfig().getBannedItems().contains(material)
                && !material.isLegacy();
    }
}
