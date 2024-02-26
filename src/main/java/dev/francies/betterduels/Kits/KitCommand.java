package dev.francies.betterduels.Kits;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private final BetterDuels plugin;

    public KitCommand(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo i giocatori possono utilizzare questo comando!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Uso: " + ChatColor.GREEN + "/kit <nome_kit>");
            return true;
        }

        String kitName = args[0].toLowerCase();
        if (!plugin.getKitManager().kitExists(kitName)) {
            player.sendMessage(ChatColor.RED + "Il kit " + ChatColor.YELLOW + kitName + ChatColor.RED + " non esiste!");
            return true;
        }

        if (!player.hasPermission("betterduels.kit." + kitName)) {
            player.sendMessage(ChatColor.RED + "Non hai il permesso di utilizzare questo kit!");
            return true;
        }

        plugin.getKitManager().giveKit(player, kitName);
        player.sendMessage(ChatColor.GREEN + "Hai ricevuto il kit: " + ChatColor.YELLOW + kitName);
        return true;
    }
}
