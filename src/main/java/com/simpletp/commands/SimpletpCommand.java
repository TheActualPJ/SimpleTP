package com.simpletp.commands;

import com.simpletp.SimpleTPPlugin;
import org.bukkit.command.*;

import java.util.Map;

public class SimpletpCommand implements CommandExecutor {

    private final SimpleTPPlugin plugin;

    public SimpletpCommand(SimpleTPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simpletp.reload")) {
            plugin.getMessageUtil().send(sender, "admin.no-permission");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            plugin.getMessageUtil().send(sender, "general.reload");
            return true;
        }
        sender.sendMessage("Usage: /simpletp reload");
        return true;
    }
}
