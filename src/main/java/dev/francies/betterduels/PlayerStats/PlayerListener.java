package dev.francies.betterduels.PlayerStats;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Duels.DuelManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {
    DuelManager manager;

    public PlayerListener(DuelManager manager) {
      this.manager = manager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DuelManager duelManager = manager;

        // Controlla se il giocatore morto stava partecipando a un duello
        if (duelManager.isInDuel(player)) {
            // Ottieni l'altro giocatore coinvolto nel duello
            Player otherPlayer = duelManager.getOtherPlayer(player);

            // Termina il duello e ripristina gli inventari dei giocatori
            duelManager.endDuel(player, otherPlayer);

            // Messaggio di avviso sulla fine del duello
            player.sendMessage("Il duello è finito perché sei morto.");

            // Rimuovi il giocatore morto dalla lista dei partecipanti al duello
            duelManager.removePlayer(player);
        }
    }
}
