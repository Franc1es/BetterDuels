package dev.francies.betterduels.Admin;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final BetterDuels plugin;

    public ReloadCommand(BetterDuels plugin) {
        this.plugin = plugin; // Passa l'istanza del tuo plugin al costruttore
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("betterduels.admin")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + "&aConfigurazione di BetterDuels ricaricata con successo."));
        } else {
            sender.sendMessage(ChatColor.RED + "Non hai il permesso per eseguire questo comando.");
        }
        return true;
    }
}
