package me.collinb.trustshops.managers;

import me.collinb.trustshops.ContainerShop;
import me.collinb.trustshops.ContainerShopPendingAction;
import me.collinb.trustshops.enums.ContainerShopModificationType;
import me.collinb.trustshops.enums.ContainerShopTransactionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;


public class ContainerShopManager {
    private final DatabaseManager databaseManager;

    // Users who have used an interactive command but haven't selected a container yet
    private final Map<Player, ContainerShopPendingAction> awaitingInteraction;

    public ContainerShopManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        awaitingInteraction = new HashMap<>();
    }

    public ContainerShop getShopByLocation(Location location) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement queryShopStatement = connection.prepareStatement("SELECT * FROM shop WHERE world = ? AND x = ? AND y = ? AND z = ?");
            queryShopStatement.setString(1, location.getWorld().getName());
            queryShopStatement.setInt(2, location.getBlockX());
            queryShopStatement.setInt(3, location.getBlockY());
            queryShopStatement.setInt(4, location.getBlockZ());

            queryShopStatement.setMaxRows(1);
            ResultSet resultSet = queryShopStatement.executeQuery();

            Optional<ContainerShop> found = getshopQueueFromResults(resultSet).stream().findFirst();
            return found.orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean awaitInteraction(Player player, ContainerShop shop, ContainerShopModificationType type) {
        if (awaitingInteraction.containsKey(player)) {
            return false;
        } else {
            awaitingInteraction.put(player, new ContainerShopPendingAction(shop, type));
            return true;
        }
    }

    public ContainerShopPendingAction popPendingInteraction(Player player) {
        return awaitingInteraction.remove(player);
    }

    public boolean registerShop(ContainerShop shop) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement insertShopStatement = connection.prepareStatement("INSERT INTO shop (uuid, world, x, y, z, container_item, player_item, container_amount, player_amount) VALUES (?,?,?,?,?,?,?,?,?)");
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

    public boolean deleteShop(Location location) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement deleteShopStatement = connection.prepareStatement("DELETE FROM shop WHERE world = ? AND x = ? and y = ? and z = ?");
            deleteShopStatement.setString(1, location.getWorld().getName());
            deleteShopStatement.setInt(2, location.getBlockX());
            deleteShopStatement.setInt(3, location.getBlockY());
            deleteShopStatement.setInt(4, location.getBlockZ());

            deleteShopStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Queue<ContainerShop> findShopsByItem(Material item, ContainerShopTransactionType type) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement shopQuery;
            switch (type) {
                case BUY:
                    shopQuery = connection.prepareStatement("SELECT * FROM shop WHERE player_item = ?");
                    shopQuery.setString(1, item.toString());
                    break;
                case SELL:
                    shopQuery = connection.prepareStatement("SELECT * FROM shop WHERE container_item = ?");
                    shopQuery.setString(1, item.toString());
                    break;
                default:
                    shopQuery = connection.prepareStatement("SELECT * FROM shop WHERE container_item = ? OR player_item = ?");
                    shopQuery.setString(1, item.toString());
                    shopQuery.setString(2, item.toString());
                    break;
            }
            ResultSet results = shopQuery.executeQuery();
            return getshopQueueFromResults(results);
        } catch (SQLException e) {
            return new PriorityQueue<>();
        }
    }

    public Queue<ContainerShop> findShopsByPlayer(OfflinePlayer shopOwner) {
        try (Connection connection = databaseManager.getConnection()) {
            PreparedStatement shopQuery;
            shopQuery = connection.prepareStatement("SELECT * FROM shop WHERE uuid = ?");
            shopQuery.setString(1, shopOwner.getUniqueId().toString());
            ResultSet results = shopQuery.executeQuery();
            return getshopQueueFromResults(results);
        } catch (SQLException e) {
            return new PriorityQueue<>();
        }
    }

    public Queue<ContainerShop> getshopQueueFromResults(ResultSet resultSet) throws SQLException {
        Queue<ContainerShop> shopQueue = new PriorityQueue<>();
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
            ContainerShop shop = new ContainerShop(shopOwner, shopLocation, containerItem, containerAmount, playerItem, playerAmount);
            shopQueue.add(shop);
        }
        return shopQueue;
    }
}