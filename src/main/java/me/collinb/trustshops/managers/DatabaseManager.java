package me.collinb.trustshops.managers;

import me.collinb.trustshops.TrustShops;
import me.collinb.trustshops.shop.Shop;
import me.collinb.trustshops.shop.ShopTransactionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final TrustShops plugin;
    private final String TABLE_PREFIX;

    public DatabaseManager(TrustShops plugin) {
        this.plugin = plugin;
        this.TABLE_PREFIX = plugin.getPluginConfig().getTablePrefix();
        init();
    }

    public Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:plugins/TrustShops/database.db");
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
    }

    public void init() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            // Create shops table
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "shop (" +
                            "uuid VARCHAR(36) NOT NULL," +
                            "world VARCHAR(36) NOT NULL," +
                            "x INT NOT NULL," +
                            "y INT NOT NULL," +
                            "z INT NOT NULL," +
                            "container_item VARCHAR(36)," +
                            "player_item VARCHAR(36)," +
                            "container_amount INT," +
                            "player_amount INT," +
                            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (world, x, y, z, container_item, player_item)" +
                            ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean closeConnection() {
        try {
            getConnection().close();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning(e.getMessage());
            return false;
        }
    }

    public List<Shop> findShopsByLocation(Location location) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement queryShopStatement = connection.prepareStatement("SELECT * FROM " + TABLE_PREFIX + "shop WHERE world = ? AND x = ? AND y = ? AND z = ?");
            queryShopStatement.setString(1, location.getWorld().getName());
            queryShopStatement.setInt(2, location.getBlockX());
            queryShopStatement.setInt(3, location.getBlockY());
            queryShopStatement.setInt(4, location.getBlockZ());
            ResultSet resultSet = queryShopStatement.executeQuery();

            return getShopListFromResults(resultSet);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean registerShop(Shop shop) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement insertShopStatement = connection.prepareStatement("INSERT INTO " + TABLE_PREFIX + "shop (uuid, world, x, y, z, container_item, player_item, container_amount, player_amount) VALUES (?,?,?,?,?,?,?,?,?)");
            insertShopStatement.setString(1, shop.getShopOwner().getUniqueId().toString());
            insertShopStatement.setString(2, shop.getShopLocation().getWorld().getName());
            insertShopStatement.setInt(3, shop.getShopLocation().getBlockX());
            insertShopStatement.setInt(4, shop.getShopLocation().getBlockY());
            insertShopStatement.setInt(5, shop.getShopLocation().getBlockZ());
            insertShopStatement.setString(6, shop.getContainerItem().toString());
            insertShopStatement.setString(7, shop.getPlayerItem().toString());
            insertShopStatement.setInt(8, shop.getContainerAmount());
            insertShopStatement.setInt(9, shop.getPlayerAmount());
            insertShopStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Delete a specific shop matching a Shop object.
     * @param shop Shop to delete
     * @return
     */
    public boolean deleteShop(Shop shop) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            Location location = shop.getShopLocation();
            PreparedStatement deleteShopStatement = connection.prepareStatement("DELETE FROM " + TABLE_PREFIX + "shop WHERE uuid = ? and world = ? AND x = ? AND y = ? AND z = ? AND player_item = ? AND container_item = ? AND player_amount = ? AND container_amount = ?");
            deleteShopStatement.setString(1, shop.getShopOwner().getUniqueId().toString());
            deleteShopStatement.setString(2, location.getWorld().getName());
            deleteShopStatement.setInt(3, location.getBlockX());
            deleteShopStatement.setInt(4, location.getBlockY());
            deleteShopStatement.setInt(5, location.getBlockZ());
            deleteShopStatement.setString(6, shop.getPlayerItem().toString());
            deleteShopStatement.setString(7, shop.getContainerItem().toString());
            deleteShopStatement.setInt(8, shop.getPlayerAmount());
            deleteShopStatement.setInt(9, shop.getContainerAmount());

            deleteShopStatement.executeUpdate();
            deleteShopStatement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Delete ALL shops at a specific location (block).
     * @param location Location of the container
     * @return Number of shops deleted
     */
    public int deleteShopsByLocation(Location location) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement deleteShopStatement = connection.prepareStatement("DELETE FROM " + TABLE_PREFIX + "shop WHERE world = ? AND x = ? AND y = ? AND z = ?");
            deleteShopStatement.setString(1, location.getWorld().getName());
            deleteShopStatement.setInt(2, location.getBlockX());
            deleteShopStatement.setInt(3, location.getBlockY());
            deleteShopStatement.setInt(4, location.getBlockZ());
            return deleteShopStatement.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Delete ALL shops owned by a specific player.
     * @param player Player to have shops deleted for
     * @return Number of shops deleted
     */
    public int deleteShopsByPlayer(OfflinePlayer player) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement deleteShopStatement = connection.prepareStatement("DELETE FROM " + TABLE_PREFIX + "shop WHERE uuid = ?");
            deleteShopStatement.setString(1, player.getUniqueId().toString());
            return deleteShopStatement.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Get a list of shops buying or selling a specific item
     * @param item Item to be searched for
     * @param type Query type: either BUY or SELL
     * @return List of Shops buying or selling a specific item. Not paginated and in all worlds
     */
    public List<Shop> findShopsByItem(Material item, ShopTransactionType type) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement shopQuery;
            switch (type) {
                case BUY:
                    shopQuery = connection.prepareStatement("SELECT * FROM " + TABLE_PREFIX + "shop WHERE player_item = ?");
                    shopQuery.setString(1, item.toString());
                    break;
                case SELL:
                    shopQuery = connection.prepareStatement("SELECT * FROM " + TABLE_PREFIX + "shop WHERE container_item = ?");
                    shopQuery.setString(1, item.toString());
                    break;
                default:
                    shopQuery = connection.prepareStatement("SELECT * FROM " + TABLE_PREFIX + "shop WHERE container_item = ? OR player_item = ?");
                    shopQuery.setString(1, item.toString());
                    shopQuery.setString(2, item.toString());
                    break;
            }
            ResultSet results = shopQuery.executeQuery();
            return getShopListFromResults(results);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get a list of shops owned by a Player
     * @param shopOwner Owner of the shops
     * @return List of Shops owned by the given player. Not paginated and includes all worlds
     */
    public List<Shop> findShopsByPlayer(OfflinePlayer shopOwner) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement shopQuery;
            shopQuery = connection.prepareStatement("SELECT * FROM " + TABLE_PREFIX + "shop WHERE uuid = ?");
            shopQuery.setString(1, shopOwner.getUniqueId().toString());
            ResultSet results = shopQuery.executeQuery();
            return getShopListFromResults(results);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    /**
     *
     * @param resultSet ResultSet from a SQL query on the shop table
     * @return List of shop objects created from the ResultShop
     */
    public List<Shop> getShopListFromResults(ResultSet resultSet) throws SQLException {
        boolean showOfflinePlayerShops = plugin.getPluginConfig().showOfflinePlayerShops();
        boolean showUnstockedPlayerShops = plugin.getPluginConfig().showUnstockedPlayerShops();
        boolean deleteShopsOnBan = plugin.getPluginConfig().deleteShopsOnBan();

        ArrayList<Shop> shops = new ArrayList<>();
        while (resultSet.next()) {
            String uuid = resultSet.getString("uuid");
            OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

            Location shopLocation = new Location(Bukkit.getWorld(resultSet.getString("world")), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));
            Material containerItem = Material.getMaterial(resultSet.getString("container_item"));
            Material playerItem = Material.getMaterial(resultSet.getString("player_item"));
            int containerAmount = resultSet.getInt("container_amount");
            int playerAmount = resultSet.getInt("player_amount");

            if (containerItem == null || playerItem == null) {
                continue;
            }

            // Create new shop object for this shop
            Shop shop = new Shop(shopOwner, shopLocation, containerItem, containerAmount, playerItem, playerAmount);

            // This is necessary for when a player is offline and doesn't get caught by PlayerQuitEvent.
            if (deleteShopsOnBan) {
                if (shopOwner.isBanned()) {
                    plugin.getDatabaseManager().deleteShop(shop);
                    continue;
                }
            }

            // If shops shouldn't be shown when unstocked, skip this shop
            if (!showUnstockedPlayerShops && shop.getStock() == 0) {
                continue;
            }

            // If shops shouldn't be shown when offline, skip this shop
            if (!showOfflinePlayerShops && !shopOwner.isOnline()) {
                continue;
            }

            shops.add(shop);
        }
        resultSet.close();
        return shops;
    }
}
