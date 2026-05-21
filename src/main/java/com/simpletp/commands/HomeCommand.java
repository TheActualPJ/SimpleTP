package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.HomeManager;
import com.simpletp.managers.TeleportManager;
import com.simpletp.models.Home;
import com.simpletp.utils.SoundUtil;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final SimpleTPPlugin plugin;
    private final HomeManager homeManager;
    private final TeleportManager teleportManager;
    private final SoundUtil soundUtil;

    public HomeCommand(SimpleTPPlugin plugin, HomeManager homeManager, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.teleportManager = teleportManager;
        this.soundUtil = new SoundUtil(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }

        // Fix: handle /home with no arguments
        if (args.length == 0) {
            plugin.getMessageUtil().send(player, "general.usage", Map.of("usage", "/home <name>"));
            return true;
        }

        String name = args[0];

        // Async lookup — never blocks main thread
        homeManager.getHome(player.getUniqueId(), name).thenAccept(home -> {
            // Result comes back on main thread via thenAccept scheduled back
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (home == null) {
                    plugin.getMessageUtil().send(player, "home.not-found", Map.of("name", name));
                    return;
                }

                Location loc = home.toLocation();
                if (loc == null) {
                    plugin.getMessageUtil().send(player, "home.world-not-found");
                    return;
                }

                plugin.getMessageUtil().send(player, "home.teleporting", Map.of("name", name));
                soundUtil.play(player, "home-teleport");

                teleportManager.startTeleport(
                        player,
                        loc,
                        () -> plugin.getMessageUtil().send(player, "teleport.complete"),
                        () -> plugin.getMessageUtil().send(player, "teleport.cancelled-move")
                );
            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) return List.of();

        String partial = args[0].toLowerCase();
        List<String> result = new ArrayList<>();

        // Fix: use cached homes only — never call .join() on main thread
        homeManager.getCachedHomes(player.getUniqueId()).stream()
                .map(Home::getName)
                .filter(n -> n.toLowerCase().startsWith(partial))
                .forEach(result::add);

        return result;
    }
}
