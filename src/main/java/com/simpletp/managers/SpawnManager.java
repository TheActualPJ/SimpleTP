package com.simpletp.managers;

import com.simpletp.SimpleTPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class SpawnManager {

    private final SimpleTPPlugin plugin;
    private File spawnFile;
    private FileConfiguration spawnConfig;
    private Location spawnLocation;

    public SpawnManager(SimpleTPPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        if (!spawnFile.exists()) {
            spawnConfig = new YamlConfiguration();
            return;
        }
        spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);

        String worldName = spawnConfig.getString("spawn.world");
        if (worldName == null) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Spawn world '" + worldName + "' not found — spawn not loaded.");
            return;
        }

        spawnLocation = new Location(
                world,
                spawnConfig.getDouble("spawn.x"),
                spawnConfig.getDouble("spawn.y"),
                spawnConfig.getDouble("spawn.z"),
                (float) spawnConfig.getDouble("spawn.yaw"),
                (float) spawnConfig.getDouble("spawn.pitch")
        );
    }

    public void setSpawn(Location location) {
        this.spawnLocation = location.clone();
        spawnConfig.set("spawn.world", location.getWorld().getName());
        spawnConfig.set("spawn.x", location.getX());
        spawnConfig.set("spawn.y", location.getY());
        spawnConfig.set("spawn.z", location.getZ());
        spawnConfig.set("spawn.yaw", location.getYaw());
        spawnConfig.set("spawn.pitch", location.getPitch());
        try {
            spawnConfig.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save spawn.yml", e);
        }
    }

    public boolean hasSpawn() {
        return spawnLocation != null;
    }

    public Location getSpawn() {
        return spawnLocation != null ? spawnLocation.clone() : null;
    }
}
