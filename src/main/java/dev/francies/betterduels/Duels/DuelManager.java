package dev.francies.betterduels.Duels;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.PlayerStats.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelManager {
    private final BetterDuels main;
    private final Map<Player, ItemStack[]> playerInventoryBackup;
    private final World duelWorld;
    private final Player player1;
    private final Player player2;
    private final Map<Player, Player> duelParticipants; // Mappa per tenere traccia dei partecipanti al duello

    public DuelManager(BetterDuels main, Player player1, Player player2) {
        this.main = main;
        this.player1 = player1;
        this.player2 = player2;
        this.playerInventoryBackup = new HashMap<>();
        this.duelParticipants = new HashMap<>();
        this.duelWorld = Bukkit.getWorld(main.getConfig().getString("world_name")); // Imposta il mondo dedicato ai duelli
        if (duelWorld == null) {
            main.getLogger().warning("Impossibile trovare il mondo dedicato ai duelli con il nome specificato nel file di configurazione.");
        }
    }

    public void startDuel(Player player1, Player player2) {
        main.getServer().getPluginManager().registerEvents(new PlayerListener(this), main);
        // Salva gli inventari dei giocatori prima di iniziare il duello
        backupInventory(player1);
        backupInventory(player2);

        // Trasporta i giocatori nel mondo dedicato ai duelli
        teleportToDuelWorld(player1);
        teleportToDuelWorld(player2);

        // Imposta il gioco dei giocatori a SURVIVAL per evitare il volo
        setGameMode(player1, GameMode.SURVIVAL);
        setGameMode(player2, GameMode.SURVIVAL);

        // Aggiungi i partecipanti al duello alla mappa dei partecipanti
        duelParticipants.put(player1, player2);
        duelParticipants.put(player2, player1);
    }

    public void endDuel(Player player1, Player player2) {
        // Ripristina gli inventari dei giocatori dopo il duello
        restoreInventory(player1);
        restoreInventory(player2);

        // Trasporta i giocatori nel mondo principale
        teleportToMainWorld(player1);
        teleportToMainWorld(player2);

        // Rimuovi i partecipanti al duello dalla mappa dei partecipanti
        duelParticipants.remove(player1);
        duelParticipants.remove(player2);
    }

    // Controlla se un giocatore è coinvolto in un duello
    public boolean isInDuel(Player player) {
        return duelParticipants.containsKey(player);
    }

    // Ottieni l'altro giocatore coinvolto nel duello
    public Player getOtherPlayer(Player player) {
        return duelParticipants.get(player);
    }

    // Rimuovi un giocatore dalla mappa dei partecipanti
    public void removePlayer(Player player) {
        duelParticipants.remove(player);
    }

    // Metodi per il backup e il ripristino degli inventari
    private void backupInventory(Player player) {
        playerInventoryBackup.put(player, player.getInventory().getContents().clone());
    }

    private void restoreInventory(Player player) {
        ItemStack[] inventoryContents = playerInventoryBackup.remove(player);
        if (inventoryContents != null) {
            player.getInventory().setContents(inventoryContents);
            player.updateInventory();
        }
    }

    // Metodi per il teletrasporto dei giocatori
    private void teleportToDuelWorld(Player player) {
        if (duelWorld != null) {
            player.teleport(duelWorld.getSpawnLocation());
        } else {
            main.getLogger().warning("Impossibile trovare il mondo dedicato ai duelli.");
        }
    }

    private void teleportToMainWorld(Player player) {
        // Trasporta il giocatore nel mondo principale (ad esempio il mondo di default)
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    private void setGameMode(Player player, GameMode gameMode) {
        player.setGameMode(gameMode);
    }
}