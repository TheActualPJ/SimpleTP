package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.HomeManager;
import com.simpletp.utils.SoundUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetHomeCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;
    private final HomeManager homeManager;
    private final SoundUtil soundUtil;

    public SetHomeCommand(SimpleTPPlugin plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.soundUtil = new SoundUtil(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }
        if (args.length != 1) {
            plugin.getMessageUtil().send(player, "general.usage", Map.of("usage", "/sethome <name>"));
            return true;
        }

        String name = args[0];
        int max = homeManager.getMaxHomes(player);

        homeManager.setHome(player, name).thenAccept(success -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    plugin.getMessageUtil().send(player, "home.set", Map.of("name", name));
                    soundUtil.play(player, "home-set");
                } else {
                    plugin.getMessageUtil().send(player, "home.max-reached", Map.of("max", String.valueOf(max)));
                }
            });
        });
        return true;
    }
}
