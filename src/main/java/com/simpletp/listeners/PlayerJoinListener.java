package com.simpletp.listeners;

import com.simpletp.SimpleTPPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final SimpleTPPlugin plugin;

    public PlayerJoinListener(SimpleTPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Pre-load homes from DB into cache so /home tab complete works instantly
        plugin.getHomeManager().preloadHomes(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Clear cache on logout to free memory
        plugin.getHomeManager().invalidateCache(event.getPlayer().getUniqueId());
    }
}
