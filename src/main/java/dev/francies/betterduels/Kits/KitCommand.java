package dev.francies.betterduels.Kits;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private final BetterDuels main;

    public KitCommand(BetterDuels main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo i giocatori possono utilizzare questo comando!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Uso: &a/kit <nome_kit>"));
            return true;
        }

        String kitName = args[0].toLowerCase();

        if (!main.getKitManager().kitExists(kitName)) {
            player.sendMessage(ChatColor.RED + "Il kit " + ChatColor.YELLOW + kitName + ChatColor.RED + " non esiste!");
            return true;
        }

        if (!player.hasPermission("btd.duels.kit." + kitName)) {
            player.sendMessage(ChatColor.RED + "Non hai il permesso di utilizzare questo kit!");
            return true;
        }

        main.getKitManager().giveKit(player, kitName);
        player.sendMessage(ChatColor.GREEN + "Hai ricevuto il kit: " + ChatColor.YELLOW + kitName);
        return true;
    }
}
