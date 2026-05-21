package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.TPAManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;

public class TPAcceptCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;
    private final TPAManager tpaManager;

    public TPAcceptCommand(SimpleTPPlugin plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }

        if (tpaManager.getIncomingRequest(player.getUniqueId()) == null) {
            plugin.getMessageUtil().send(player, "tpa.no-request");
            return true;
        }

        tpaManager.acceptRequest(player);
        return true;
    }
}
