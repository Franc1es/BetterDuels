package dev.francies.betterduels.Duels;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
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
            sender.sendMessage("Solo i giocatori possono eseguire questo comando.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage("Utilizzo corretto: /duel <nome_giocatore>");
            return true;
        }

        // Controlla se il giocatore è già in un duello
        if (plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage("Non puoi iniziare un nuovo duello mentre sei già in uno.");
            return true;
        }

        String targetPlayerName = args[0];
        Player targetPlayer = plugin.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage("Il giocatore specificato non è online.");
            return true;
        }

        if (targetPlayer.equals(sender)) {
            player.sendMessage("Non puoi sfidare te stesso.");
            return true;
        }

        // Controlla se il giocatore target è già in un duello
        if (plugin.getDuelManager().isInDuel(targetPlayer)) {
            player.sendMessage("Il giocatore specificato è già impegnato in un duello.");
            return true;
        }

        // Avvia il duello
        plugin.getDuelManager().startDuel(player, targetPlayer);
        return true;
    }
}
