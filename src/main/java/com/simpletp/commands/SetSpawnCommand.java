package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.SpawnManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetSpawnCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;
    private final SpawnManager spawnManager;

    public SetSpawnCommand(SimpleTPPlugin plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }
        if (!player.hasPermission("simpletp.setspawn")) {
            plugin.getMessageUtil().send(player, "admin.no-permission");
            return true;
        }

        spawnManager.setSpawn(player.getLocation());
        plugin.getMessageUtil().send(player, "spawn.set",
                Map.of(
                    "world", player.getWorld().getName(),
                    "x", String.valueOf((int) player.getLocation().getX()),
                    "y", String.valueOf((int) player.getLocation().getY()),
                    "z", String.valueOf((int) player.getLocation().getZ())
                ));
        return true;
    }
}
