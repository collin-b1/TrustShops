package me.collinb.trustshops.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    public DatabaseManager() {
        init();
    }
    public Connection getConnection()  {
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
                "CREATE TABLE IF NOT EXISTS shop (" +
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
                    "PRIMARY KEY (world, x, y, z)" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
