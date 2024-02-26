package dev.francies.betterduels.PlayerStats;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Duels.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final DuelManager duelManager;
    private BetterDuels plugin;

    public PlayerListener(DuelManager duelManager, BetterDuels plugin) {
        this.duelManager = duelManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (duelManager.isInDuel(player)) {
            Player otherPlayer = duelManager.getOtherPlayer(player);
            duelManager.endDuel(player, otherPlayer);
            otherPlayer.sendMessage("Hai vinto il duello!");
            player.sendMessage("Hai perso il duello!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (duelManager.isInDuel(player)) {
            Player otherPlayer = duelManager.getOtherPlayer(player);
            // Termina il duello e dichiara l'altro giocatore vincitore
            duelManager.endDuel(player, otherPlayer);
            otherPlayer.sendMessage("L'altro giocatore ha abbandonato il gioco. Hai vinto il duello!");
            // Banna il giocatore che ha abbandonato
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), "Hai abbandonato un duello", null, "DuelManager");
            String command = plugin.getConfig().getString("punishment-command");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
