package com.simpletp.listeners;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.SpawnManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpawnListener implements Listener {

    private final SimpleTPPlugin plugin;
    private final SpawnManager spawnManager;
    // Track players who died without a bed so we know to redirect their respawn
    private final Set<UUID> noBedDeaths = new HashSet<>();

    public SpawnListener(SimpleTPPlugin plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    // First join — teleport to spawn
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFirstJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            if (!spawnManager.hasSpawn()) return;
            Location spawn = spawnManager.getSpawn();
            // Small delay so the world is fully loaded for the player
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event.getPlayer().isOnline()) {
                    event.getPlayer().teleportAsync(spawn);
                    plugin.getMessageUtil().send(event.getPlayer(), "spawn.welcome");
                }
            }, 20L);
        }
        // Returning players: do nothing — server restores their last location automatically
    }

    // Track deaths where player has no bed/respawn anchor
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        // getBedSpawnLocation() returns null if no bed or respawn anchor is set
        if (player.getBedSpawnLocation() == null) {
            noBedDeaths.add(player.getUniqueId());
        }
    }

    // On respawn, redirect to plugin spawn if they had no bed
    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!spawnManager.hasSpawn()) return;
        if (!noBedDeaths.remove(event.getPlayer().getUniqueId())) return;
        // Only override if the event isn't already sending them to a bed/anchor
        if (event.isBedSpawn() || event.isAnchorSpawn()) return;

        event.setRespawnLocation(spawnManager.getSpawn());
        plugin.getMessageUtil().send(event.getPlayer(), "spawn.respawned");
    }
}
