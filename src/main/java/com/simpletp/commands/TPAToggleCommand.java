package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import com.simpletp.managers.TPAManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TPAToggleCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;
    private final TPAManager tpaManager;

    public TPAToggleCommand(SimpleTPPlugin plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "general.player-only");
            return true;
        }

        boolean nowBlocked = tpaManager.toggleTPA(player.getUniqueId());
        if (nowBlocked) {
            plugin.getMessageUtil().send(player, "tpa.toggle-off");
        } else {
            plugin.getMessageUtil().send(player, "tpa.toggle-on");
        }
        return true;
    }
}
