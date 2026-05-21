package com.simpletp.utils;

import com.simpletp.SimpleTPPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    private final SimpleTPPlugin plugin;

    public SoundUtil(SimpleTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player player, String key) {
        if (!plugin.getConfig().getBoolean("sounds.enabled", true)) return;
        String soundName = plugin.getConfig().getString("sounds." + key, null);
        if (soundName == null) return;
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger().warning("Invalid sound in config: sounds." + key + " = " + soundName);
        }
    }
}
