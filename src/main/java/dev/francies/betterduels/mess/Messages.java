package dev.francies.betterduels.mess;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class Messages {
    private static final Map<String, String> messages = new HashMap<>();

    public static void loadMessages(BetterDuels plugin) {
        FileConfiguration config = plugin.getConfig();
        if (config.getConfigurationSection("messages") != null) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                String path = "messages." + key;
                String message = config.getString(path, "&cMessage not found: " + key);
                messages.put(key, ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }


    public static String get(String key) {

        String prefix = messages.getOrDefault("prefix", "&7[&6BetterDuels&7] ");

        return messages.getOrDefault(key, ChatColor.translateAlternateColorCodes('&', prefix + "&cMessage not found: " + key));
    }
}
