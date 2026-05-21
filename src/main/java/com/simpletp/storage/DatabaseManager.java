package com.simpletp.storage;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.models.Home;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final SimpleTPPlugin plugin;
    private Connection connection;
    private final Object lock = new Object();

    public DatabaseManager(SimpleTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        synchronized (lock) {
            try {
                plugin.getDataFolder().mkdirs();

                // Explicitly load the shaded H2 driver class — required when shading
                Class.forName("com.simpletp.libs.h2.Driver");

                File dbFile = new File(plugin.getDataFolder(), "homes");
                String url = "jdbc:h2:file:" + dbFile.getAbsolutePath()
                        + ";MODE=MySQL;AUTO_SERVER=FALSE;DB_CLOSE_ON_EXIT=FALSE";
                connection = DriverManager.getConnection(url, "sa", "");
                connection.setAutoCommit(true);
                createTable();
                plugin.getLogger().info("Database connected successfully.");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().severe("H2 driver class not found — jar may not have shaded correctly!");
                plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
            }
        }
    }

    private void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS homes (
                    uuid  VARCHAR(36)  NOT NULL,
                    name  VARCHAR(64)  NOT NULL,
                    world VARCHAR(64)  NOT NULL,
                    x     DOUBLE       NOT NULL,
                    y     DOUBLE       NOT NULL,
                    z     DOUBLE       NOT NULL,
                    yaw   FLOAT        NOT NULL,
                    pitch FLOAT        NOT NULL,
                    PRIMARY KEY (uuid, name)
                )
            """);
        }
    }

    private boolean ensureConnection() {
        synchronized (lock) {
            try {
                if (connection == null || connection.isClosed()) {
                    plugin.getLogger().warning("DB connection lost — reconnecting...");
                    initialize();
                }
                return connection != null && !connection.isClosed();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Cannot check DB connection", e);
                return false;
            }
        }
    }

    public void close() {
        synchronized (lock) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    plugin.getLogger().info("Database connection closed.");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing database", e);
            }
        }
    }

    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        CompletableFuture<List<Home>> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                if (!ensureConnection()) {
                    plugin.getLogger().severe("Cannot load homes — no DB connection.");
                    future.complete(new ArrayList<>());
                    return;
                }
                List<Home> homes = new ArrayList<>();
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT * FROM homes WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        homes.add(new Home(
                                UUID.fromString(rs.getString("uuid")),
                                rs.getString("name"),
                                rs.getString("world"),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch")
                        ));
                    }
                    future.complete(homes);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to load homes for " + uuid, e);
                    future.complete(new ArrayList<>());
                }
            }
        });
        return future;
    }

    public void saveHome(Home home) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                if (!ensureConnection()) return;
                try (PreparedStatement ps = connection.prepareStatement(
                        "MERGE INTO homes (uuid, name, world, x, y, z, yaw, pitch) " +
                        "KEY(uuid, name) VALUES (?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, home.getOwnerUUID().toString());
                    ps.setString(2, home.getName());
                    ps.setString(3, home.getWorld());
                    ps.setDouble(4, home.getX());
                    ps.setDouble(5, home.getY());
                    ps.setDouble(6, home.getZ());
                    ps.setFloat(7, home.getYaw());
                    ps.setFloat(8, home.getPitch());
                    ps.executeUpdate();
                    plugin.getLogger().info("[DB] Saved home '" + home.getName()
                            + "' for " + home.getOwnerUUID());
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE,
                            "Failed to save home '" + home.getName() + "'", e);
                }
            }
        });
    }

    public void deleteHome(UUID uuid, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (lock) {
                if (!ensureConnection()) return;
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM homes WHERE uuid = ? AND name = ?")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, name);
                    ps.executeUpdate();
                    plugin.getLogger().info("[DB] Deleted home '" + name + "' for " + uuid);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE,
                            "Failed to delete home '" + name + "'", e);
                }
            }
        });
    }
}
