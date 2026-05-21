package com.simpletp.managers;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.models.Home;
import com.simpletp.storage.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {

    private final SimpleTPPlugin plugin;
    private final DatabaseManager db;
    // Cache: UUID -> (homeName lowercase -> Home)
    private final Map<UUID, Map<String, Home>> cache = new ConcurrentHashMap<>();

    public HomeManager(SimpleTPPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    // Always loads fresh from DB, then updates cache
    private CompletableFuture<Map<String, Home>> loadFromDB(UUID uuid) {
        return db.getHomes(uuid).thenApply(list -> {
            Map<String, Home> map = new LinkedHashMap<>();
            for (Home h : list) map.put(h.getName().toLowerCase(), h);
            cache.put(uuid, map);
            return map;
        });
    }

    // Use cache if available, else load from DB
    private CompletableFuture<Map<String, Home>> getOrLoad(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(cache.get(uuid));
        }
        return loadFromDB(uuid);
    }

    // Safe for tab completion on main thread — uses cache only
    public Collection<Home> getCachedHomes(UUID uuid) {
        Map<String, Home> map = cache.get(uuid);
        return map == null ? List.of() : Collections.unmodifiableCollection(map.values());
    }

    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        return getOrLoad(uuid).thenApply(map -> new ArrayList<>(map.values()));
    }

    public CompletableFuture<Home> getHome(UUID uuid, String name) {
        return getOrLoad(uuid).thenApply(map -> map.get(name.toLowerCase()));
    }

    public CompletableFuture<Boolean> setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        return getOrLoad(uuid).thenApply(map -> {
            int max = getMaxHomes(player);
            boolean isNew = !map.containsKey(name.toLowerCase());
            if (isNew && map.size() >= max) return false;

            Home home = new Home(uuid, name, player.getLocation());
            // Update cache first so it's immediately visible
            map.put(name.toLowerCase(), home);
            cache.put(uuid, map);
            // Persist to DB
            db.saveHome(home);
            plugin.getLogger().info("[HomeManager] Set home '" + name + "' for " + player.getName());
            return true;
        });
    }

    public CompletableFuture<Boolean> deleteHome(UUID uuid, String name) {
        return getOrLoad(uuid).thenApply(map -> {
            if (!map.containsKey(name.toLowerCase())) return false;
            map.remove(name.toLowerCase());
            db.deleteHome(uuid, name);
            return true;
        });
    }

    public int getMaxHomes(Player player) {
        if (player.hasPermission("simpletp.homes.unlimited")) return Integer.MAX_VALUE;
        if (player.hasPermission("simpletp.homes.25")) return 25;
        if (player.hasPermission("simpletp.homes.10")) return 10;
        if (player.hasPermission("simpletp.homes.5")) return 5;
        return plugin.getConfig().getInt("default-max-homes", 10);
    }

    // Called on player join to warm the cache from DB
    public void preloadHomes(UUID uuid) {
        loadFromDB(uuid).thenAccept(map ->
            plugin.getLogger().info("[HomeManager] Loaded " + map.size()
                    + " homes for " + uuid)
        );
    }

    public void invalidateCache(UUID uuid) {
        cache.remove(uuid);
    }
}
