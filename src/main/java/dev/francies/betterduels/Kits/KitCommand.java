package dev.francies.betterduels.Kits;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Mess.Messages;
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("no-perm")));
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("kit-usage-command"))); // Assicurati che "kit-usage-command" sia definito nel tuo config
            return true;
        }

        String kitName = args[0].toLowerCase();
        if (!plugin.getKitManager().kitExists(kitName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("kit-not-exist"))); // Assicurati che "kit-not-exist" sia definito nel tuo config
            return true;
        }

        if (!player.hasPermission("betterduels.kit." + kitName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("no-perm-kit"))); // Usa il messaggio già definito per i permessi
            return true;
        }

        plugin.getKitManager().giveKit(player, kitName);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("kit-received").replace("%kit%", kitName))); // Assicurati che "kit-received" sia definito nel tuo config
        return true;
    }
}
