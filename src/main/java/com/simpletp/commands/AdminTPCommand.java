package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminTPCommand implements CommandExecutor, TabCompleter {

    private final SimpleTPPlugin plugin;

    public AdminTPCommand(SimpleTPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("simpletp.admin")) {
            plugin.getMessageUtil().send(player, "admin.no-permission");
            return true;
        }
        if (args.length != 1) {
            plugin.getMessageUtil().send(player, "general.usage", Map.of("usage", "/tp <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            plugin.getMessageUtil().send(player, "admin.player-not-found", Map.of("name", args[0]));
            return true;
        }

        player.teleport(target.getLocation());
        plugin.getMessageUtil().send(player, "admin.teleported-to", Map.of("target", target.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
