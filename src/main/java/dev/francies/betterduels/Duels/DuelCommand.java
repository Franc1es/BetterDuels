package dev.francies.betterduels.Duels;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Kits.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    private final BetterDuels main;
    private final KitManager kitManager;

    public DuelCommand(BetterDuels main) {
        this.main = main;
        this.kitManager = main.getKitManager();
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

        String targetPlayerName = args[0];
        Player targetPlayer = main.getServer().getPlayer(targetPlayerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage("Il giocatore specificato non è online.");
            return true;
        }

        if (targetPlayer == sender){
            player.sendMessage("Non puoi sfidare te stesso");
            return true;
        }

        // Avvia il duello tra il giocatore e il target
        DuelManager duelManager = new DuelManager(main, player, targetPlayer);
        duelManager.startDuel(player, targetPlayer );

        return true;
    }
}
