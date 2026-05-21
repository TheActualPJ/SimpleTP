package com.simpletp.utils;

import com.simpletp.SimpleTPPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public class MessageUtil {

    private final SimpleTPPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration messages;
    private String prefix;

    public MessageUtil(SimpleTPPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
        prefix = plugin.getConfig().getString("messages.prefix",
                "<aqua><bold>SimpleTP</bold></aqua> <dark_gray>»</dark_gray>");
    }

    public Component get(String key, Map<String, String> replacements) {
        String raw = messages.getString(key, "<red>Missing message: " + key + "</red>");
        raw = raw.replace("<prefix>", prefix);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            raw = raw.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return miniMessage.deserialize(raw);
    }

    public Component get(String key) {
        return get(key, Map.of());
    }

    public void send(Audience audience, String key, Map<String, String> replacements) {
        audience.sendMessage(get(key, replacements));
    }

    public void send(Audience audience, String key) {
        send(audience, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> replacements) {
        sender.sendMessage(get(key, replacements));
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void sendActionBar(Audience audience, String key, Map<String, String> replacements) {
        audience.sendActionBar(get(key, replacements));
    }

    public void sendActionBar(Audience audience, String key) {
        sendActionBar(audience, key, Map.of());
    }
}
