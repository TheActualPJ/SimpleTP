package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.HomeManager;
import com.simpletp.models.Home;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class HomesCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;
    private final HomeManager homeManager;

    public HomesCommand(SimpleTPPlugin plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }

        int max = homeManager.getMaxHomes(player);

        homeManager.getHomes(player.getUniqueId()).thenAccept(homes -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (homes.isEmpty()) {
                    plugin.getMessageUtil().send(player, "home.list-empty");
                    return;
                }

                plugin.getMessageUtil().send(player, "home.list-header",
                        Map.of("count", String.valueOf(homes.size()), "max",
                                max == Integer.MAX_VALUE ? "∞" : String.valueOf(max)));

                for (Home home : homes) {
                    plugin.getMessageUtil().send(player, "home.list-entry", Map.of(
                            "name", home.getName(),
                            "world", home.getWorld(),
                            "x", String.valueOf((int) home.getX()),
                            "y", String.valueOf((int) home.getY()),
                            "z", String.valueOf((int) home.getZ())
                    ));
                }
            });
        });
        return true;
    }
}
