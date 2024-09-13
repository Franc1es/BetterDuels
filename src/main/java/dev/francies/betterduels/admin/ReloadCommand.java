package dev.francies.betterduels.admin;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.mess.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final BetterDuels plugin;

    public ReloadCommand(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("betterduels.admin")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + "&aBetterDuels reloaded successfully."));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("no-perm")));
        }
        return true;
    }
}
