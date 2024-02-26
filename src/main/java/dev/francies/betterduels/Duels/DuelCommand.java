package dev.francies.betterduels.Duels;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Mess.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    private final BetterDuels plugin;

    public DuelCommand(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("no-perm")));
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("duel-usage-command")));
            return true;
        }


        if (plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("no-duel-command-during-duel")));
            return true;
        }

        String targetPlayerName = args[0];
        Player targetPlayer = plugin.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("player-not-online")));
            return true;
        }

        if (targetPlayer.equals(sender)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("error-duel-youself")));
            return true;
        }


        if (plugin.getDuelManager().isInDuel(targetPlayer)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("already-in-a-duel")));
            return true;
        }


        plugin.getDuelManager().startDuel(player, targetPlayer);

        return true;
    }
}
