package com.simpletp.managers;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.models.TPARequest;
import com.simpletp.utils.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TPAManager {

    private final SimpleTPPlugin plugin;
    private final TeleportManager teleportManager;
    private final SoundUtil soundUtil;

    private final Map<UUID, TPARequest> outgoing = new ConcurrentHashMap<>();
    private final Map<UUID, TPARequest> incoming = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    // Players who have disabled TPA requests
    private final Set<UUID> tpaBlocked = ConcurrentHashMap.newKeySet();

    public TPAManager(SimpleTPPlugin plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
        this.soundUtil = new SoundUtil(plugin);
        startExpiryTask();
    }

    public enum SendResult { SUCCESS, ALREADY_HAS_REQUEST, ON_COOLDOWN, CANNOT_SELF, TARGET_BLOCKED }

    /** Returns true if TPA is now blocked, false if now enabled */
    public boolean toggleTPA(UUID uuid) {
        if (tpaBlocked.contains(uuid)) {
            tpaBlocked.remove(uuid);
            return false;
        } else {
            tpaBlocked.add(uuid);
            return true;
        }
    }

    public boolean isTpaBlocked(UUID uuid) {
        return tpaBlocked.contains(uuid);
    }

    public SendResult sendRequest(Player sender, Player target, TPARequest.Type type) {
        if (sender.getUniqueId().equals(target.getUniqueId())) return SendResult.CANNOT_SELF;

        // Bypass permission skips cooldown AND blocked check
        if (!sender.hasPermission("simpletp.bypass")) {
            int cooldownSec = plugin.getConfig().getInt("tpa.cooldown", 30);
            long lastSent = cooldowns.getOrDefault(sender.getUniqueId(), 0L);
            long elapsed = (System.currentTimeMillis() - lastSent) / 1000;
            if (elapsed < cooldownSec) return SendResult.ON_COOLDOWN;

            if (tpaBlocked.contains(target.getUniqueId())) return SendResult.TARGET_BLOCKED;
        }

        if (outgoing.containsKey(sender.getUniqueId())) return SendResult.ALREADY_HAS_REQUEST;

        TPARequest request = new TPARequest(sender.getUniqueId(), target.getUniqueId(), type);
        outgoing.put(sender.getUniqueId(), request);
        incoming.put(target.getUniqueId(), request);
        cooldowns.put(sender.getUniqueId(), System.currentTimeMillis());
        return SendResult.SUCCESS;
    }

    public TPARequest getIncomingRequest(UUID targetUUID) { return incoming.get(targetUUID); }
    public TPARequest getOutgoingRequest(UUID senderUUID) { return outgoing.get(senderUUID); }

    public void acceptRequest(Player target) {
        TPARequest request = incoming.remove(target.getUniqueId());
        if (request == null) return;
        outgoing.remove(request.getSenderUUID());

        Player sender = Bukkit.getPlayer(request.getSenderUUID());
        if (sender == null || !sender.isOnline()) {
            plugin.getMessageUtil().send(target, "admin.player-not-found", Map.of("name", "sender"));
            return;
        }

        Player teleporter  = request.getType() == TPARequest.Type.TPA ? sender : target;
        Player destination = request.getType() == TPARequest.Type.TPA ? target : sender;

        plugin.getMessageUtil().send(target, "tpa.request-accepted");
        plugin.getMessageUtil().send(sender, "tpa.request-accepted-notify", Map.of("player", target.getName()));
        soundUtil.play(sender, "teleport-start");
        soundUtil.play(target, "teleport-start");

        teleportManager.startTeleport(
                teleporter,
                destination.getLocation().clone(),
                () -> plugin.getMessageUtil().send(teleporter, "teleport.complete"),
                () -> plugin.getMessageUtil().send(teleporter, "teleport.cancelled-move")
        );
    }

    public boolean denyRequest(Player target) {
        TPARequest request = incoming.remove(target.getUniqueId());
        if (request == null) return false;
        outgoing.remove(request.getSenderUUID());
        Player sender = Bukkit.getPlayer(request.getSenderUUID());
        if (sender != null && sender.isOnline())
            plugin.getMessageUtil().send(sender, "tpa.request-denied-notify", Map.of("player", target.getName()));
        return true;
    }

    public boolean cancelRequest(Player sender) {
        TPARequest request = outgoing.remove(sender.getUniqueId());
        if (request == null) return false;
        incoming.remove(request.getTargetUUID());
        return true;
    }

    public long getCooldownRemaining(UUID senderUUID) {
        int cooldownSec = plugin.getConfig().getInt("tpa.cooldown", 30);
        long lastSent = cooldowns.getOrDefault(senderUUID, 0L);
        long elapsed = (System.currentTimeMillis() - lastSent) / 1000;
        return Math.max(0, cooldownSec - elapsed);
    }

    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int timeoutSec = plugin.getConfig().getInt("tpa.request-timeout", 60);
            outgoing.entrySet().removeIf(entry -> {
                TPARequest req = entry.getValue();
                if (!req.isExpired(timeoutSec)) return false;
                incoming.remove(req.getTargetUUID());
                Player sender = Bukkit.getPlayer(req.getSenderUUID());
                Player tgt    = Bukkit.getPlayer(req.getTargetUUID());
                if (sender != null) plugin.getMessageUtil().send(sender, "tpa.request-expired",
                        Map.of("target", tgt != null ? tgt.getName() : "Unknown"));
                if (tgt != null) plugin.getMessageUtil().send(tgt, "tpa.request-expired-target",
                        Map.of("sender", sender != null ? sender.getName() : "Unknown"));
                return true;
            });
        }, 20L, 20L);
    }
}
