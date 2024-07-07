package me.collinb.trustshops;

import com.google.common.collect.ImmutableSet;
import me.collinb.trustshops.commands.CommandCreateShop;
import me.collinb.trustshops.commands.CommandDeleteShop;
import me.collinb.trustshops.commands.CommandShops;
import me.collinb.trustshops.listeners.PlayerListener;
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
    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        saveDefaultConfig();

        databaseManager = new DatabaseManager();
        shopManager = new ContainerShopManager(databaseManager);
        chatManager = new ChatManager();

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
        getLogger().info("Loaded containers: " + ALLOWED_CONTAINERS.toString());

        // Register events
        getServer().getPluginManager().registerEvents(new PlayerListener(shopManager), this);

        // Register commands
        CommandCreateShop commandCreateShop = new CommandCreateShop(shopManager);
        Objects.requireNonNull(this.getCommand("createshop")).setExecutor(commandCreateShop);
        Objects.requireNonNull(this.getCommand("createshop")).setTabCompleter(commandCreateShop);

        Objects.requireNonNull(this.getCommand("deleteshop")).setExecutor(new CommandDeleteShop(shopManager));
        Objects.requireNonNull(this.getCommand("shops")).setExecutor(new CommandShops(shopManager, chatManager));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
