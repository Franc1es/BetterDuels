package dev.francies.betterduels.admin;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.entity.Player;


public class DuelStatsRemove implements CommandExecutor {
    private final BetterDuels plugin;
    private FileConfiguration leaderboardConfig;

    public DuelStatsRemove(BetterDuels plugin) {
        this.plugin = plugin;
        this.leaderboardConfig = plugin.getLeaderboardConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm")));
            return true;
        }
        if (!sender.hasPermission("betterduels.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm")));
            return true;
        }

        Player player = (Player) sender;
        int limit = plugin.getConfig().getInt("leaderboard.player-to-show") + 1;
        int x = plugin.getLeaderboardConfig().getInt("location.x");
        int y = plugin.getLeaderboardConfig().getInt("location.y");
        int z = plugin.getLeaderboardConfig().getInt("location.z");
        String com = "execute as @a[x="+ x +",y="+ y +",z="+ z +"] run kill @e[sort=nearest,limit= "+ limit +",type=minecraft:armor_stand]";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), com);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), com);
        clearLeaderboardConfig();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.leaderboard-eliminated")));



        return true;
    }

    public void clearLeaderboardConfig() {

        FileConfiguration config = plugin.getLeaderboardConfig();

        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }

        plugin.saveLeaderboardConfig();
    }

}
