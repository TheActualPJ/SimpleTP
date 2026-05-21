package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.TPAManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TPACancelCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;
    private final TPAManager tpaManager;

    public TPACancelCommand(SimpleTPPlugin plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }

        if (!tpaManager.cancelRequest(player)) {
            plugin.getMessageUtil().send(player, "tpa.no-outgoing");
            return true;
        }

        plugin.getMessageUtil().send(player, "tpa.request-cancelled");
        return true;
    }
}
