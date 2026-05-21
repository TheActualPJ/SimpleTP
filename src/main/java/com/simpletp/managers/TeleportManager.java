package com.simpletp.managers;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.utils.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    private final SimpleTPPlugin plugin;
    private final SoundUtil soundUtil;
    private final Map<UUID, PendingTeleport> pending = new ConcurrentHashMap<>();

    public TeleportManager(SimpleTPPlugin plugin) {
        this.plugin = plugin;
        this.soundUtil = new SoundUtil(plugin);
    }

    public void startTeleport(Player player, Location destination, Runnable onComplete, Runnable onCancel) {
        cancelTeleport(player.getUniqueId(), false);

        int delay = plugin.getConfig().getInt("teleport.delay", 5);
        double threshold = plugin.getConfig().getDouble("teleport.movement-threshold", 0.1);

        PendingTeleport pt = new PendingTeleport(
                player, destination, player.getLocation().clone(), threshold, onCancel
        );

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int secondsLeft = delay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    pending.remove(player.getUniqueId());
                    pt.cancel();
                    return;
                }

                if (secondsLeft > 0) {
                    // Show actionbar countdown
                    plugin.getMessageUtil().sendActionBar(player, "teleport.countdown",
                            Map.of("seconds", String.valueOf(secondsLeft)));
                    soundUtil.play(player, "teleport-start");
                    secondsLeft--;
                    return;
                }

                // Countdown finished — remove from pending and cancel task first
                pending.remove(player.getUniqueId());
                pt.cancel();

                // Use teleportAsync so chunks load properly before moving the player
                player.teleportAsync(destination).thenAccept(success -> {
                    if (success) {
                        // Back on main thread for messages/particles/sounds
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            spawnParticles(player);
                            soundUtil.play(player, "teleport-complete");
                            onComplete.run();
                        });
                    } else {
                        plugin.getServer().getScheduler().runTask(plugin, () ->
                                plugin.getMessageUtil().send(player, "teleport.cancelled-move")
                        );
                    }
                });
            }
        }, 20L, 20L); // Start after 1 second (20L), tick every second

        pt.setTask(task);
        pending.put(player.getUniqueId(), pt);

        // Send the first countdown message immediately
        plugin.getMessageUtil().sendActionBar(player, "teleport.countdown",
                Map.of("seconds", String.valueOf(delay)));
    }

    public void cancelTeleport(UUID uuid, boolean notify) {
        PendingTeleport pt = pending.remove(uuid);
        if (pt == null) return;
        pt.cancel();
        if (notify && pt.getPlayer().isOnline()) {
            pt.getOnCancel().run();
        }
    }

    public boolean hasPendingTeleport(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public PendingTeleport getPending(UUID uuid) {
        return pending.get(uuid);
    }

    public void cancelAll() {
        pending.forEach((uuid, pt) -> pt.cancel());
        pending.clear();
    }

    private void spawnParticles(Player player) {
        try {
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 40, 0.5, 1.0, 0.5, 0.1);
        } catch (Exception ignored) {}
    }

    // ---- Inner class ----

    public static class PendingTeleport {
        private final Player player;
        private final Location destination;
        private final Location origin;
        private final double threshold;
        private final Runnable onCancel;
        private BukkitTask task;

        public PendingTeleport(Player player, Location destination, Location origin,
                               double threshold, Runnable onCancel) {
            this.player = player;
            this.destination = destination;
            this.origin = origin;
            this.threshold = threshold;
            this.onCancel = onCancel;
        }

        public boolean hasMoved(Location current) {
            return Math.abs(current.getX() - origin.getX()) > threshold
                    || Math.abs(current.getY() - origin.getY()) > threshold
                    || Math.abs(current.getZ() - origin.getZ()) > threshold;
        }

        public void setTask(BukkitTask task) { this.task = task; }
        public void cancel() { if (task != null && !task.isCancelled()) task.cancel(); }

        public Player getPlayer() { return player; }
        public Location getOrigin() { return origin; }
        public Runnable getOnCancel() { return onCancel; }
    }
}
