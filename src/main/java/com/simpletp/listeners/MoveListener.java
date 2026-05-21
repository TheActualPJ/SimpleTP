package com.simpletp.listeners;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.TeleportManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private final SimpleTPPlugin plugin;
    private final TeleportManager teleportManager;

    public MoveListener(SimpleTPPlugin plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-move", true)) return;
        if (!teleportManager.hasPendingTeleport(event.getPlayer().getUniqueId())) return;

        TeleportManager.PendingTeleport pt = teleportManager.getPending(event.getPlayer().getUniqueId());
        if (pt == null) return;

        // Only cancel on X/Y/Z change, not just looking around
        if (pt.hasMoved(event.getTo())) {
            teleportManager.cancelTeleport(event.getPlayer().getUniqueId(), false);
            plugin.getMessageUtil().send(event.getPlayer(), "teleport.cancelled-move");
        }
    }
}
