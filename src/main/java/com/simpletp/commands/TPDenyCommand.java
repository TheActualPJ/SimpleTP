package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.TPAManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TPDenyCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;
    private final TPAManager tpaManager;

    public TPDenyCommand(SimpleTPPlugin plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }

        if (!tpaManager.denyRequest(player)) {
            plugin.getMessageUtil().send(player, "tpa.no-request");
            return true;
        }

        plugin.getMessageUtil().send(player, "tpa.request-denied");
        return true;
    }
}
