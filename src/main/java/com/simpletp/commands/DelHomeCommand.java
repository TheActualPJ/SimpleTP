package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.HomeManager;
import com.simpletp.models.Home;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelHomeCommand implements CommandExecutor, TabCompleter {

    private final SimpleTPPlugin plugin;
    private final HomeManager homeManager;

    public DelHomeCommand(SimpleTPPlugin plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }
        if (args.length != 1) {
            plugin.getMessageUtil().send(player, "general.usage", Map.of("usage", "/delhome <name>"));
            return true;
        }

        String name = args[0];
        homeManager.deleteHome(player.getUniqueId(), name).thenAccept(deleted -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (deleted) {
                    plugin.getMessageUtil().send(player, "home.deleted", Map.of("name", name));
                } else {
                    plugin.getMessageUtil().send(player, "home.not-found", Map.of("name", name));
                }
            });
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) return List.of();
        String partial = args[0].toLowerCase();
        List<String> result = new ArrayList<>();
        // Use cache only — never .join() on main thread
        homeManager.getCachedHomes(player.getUniqueId()).stream()
                .map(Home::getName)
                .filter(n -> n.toLowerCase().startsWith(partial))
                .forEach(result::add);
        return result;
    }
}
