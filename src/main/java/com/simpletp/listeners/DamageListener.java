package com.simpletp.listeners;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.TeleportManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public class DamageListener implements Listener {

    private final SimpleTPPlugin plugin;
    private final TeleportManager teleportManager;

    public DamageListener(SimpleTPPlugin plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-damage", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!teleportManager.hasPendingTeleport(player.getUniqueId())) return;

        teleportManager.cancelTeleport(player.getUniqueId(), false);
        plugin.getMessageUtil().send(player, "teleport.cancelled-damage");
    }
}
