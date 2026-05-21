package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.TPAManager;
import com.simpletp.models.TPARequest;
import com.simpletp.utils.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TPACommand implements CommandExecutor, TabCompleter {

    private final SimpleTPPlugin plugin;
    private final TPAManager tpaManager;
    private final SoundUtil soundUtil;

    public TPACommand(SimpleTPPlugin plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
        this.soundUtil = new SoundUtil(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }
        if (args.length != 1) {
            plugin.getMessageUtil().send(player, "general.usage", Map.of("usage", "/tpa <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            plugin.getMessageUtil().send(player, "admin.player-not-found", Map.of("name", args[0]));
            return true;
        }

        int timeout = plugin.getConfig().getInt("tpa.request-timeout", 60);
        TPAManager.SendResult result = tpaManager.sendRequest(player, target, TPARequest.Type.TPA);

        switch (result) {
            case SUCCESS -> {
                plugin.getMessageUtil().send(player, "tpa.request-sent",
                        Map.of("target", target.getName(), "timeout", String.valueOf(timeout)));
                plugin.getMessageUtil().send(target, "tpa.request-received",
                        Map.of("sender", player.getName()));
                soundUtil.play(target, "tpa-request");
            }
            case CANNOT_SELF      -> plugin.getMessageUtil().send(player, "tpa.cannot-self");
            case ALREADY_HAS_REQUEST -> plugin.getMessageUtil().send(player, "tpa.already-sent");
            case TARGET_BLOCKED   -> plugin.getMessageUtil().send(player, "tpa.target-blocked",
                    Map.of("target", target.getName()));
            case ON_COOLDOWN      -> plugin.getMessageUtil().send(player, "tpa.on-cooldown",
                    Map.of("seconds", String.valueOf(tpaManager.getCooldownRemaining(player.getUniqueId()))));
        }
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
