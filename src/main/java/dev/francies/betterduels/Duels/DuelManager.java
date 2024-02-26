package dev.francies.betterduels.Duels;

import dev.francies.betterduels.WorldManager.DuelWorldManager;
import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.PlayerStats.PlayerListener;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelManager {
    private final BetterDuels main;
    private final Map<Player, ItemStack[]> playerInventoryBackup = new HashMap<>();
    private final World duelWorld;
    private final World endDuelWorld;
    private final Map<Player, Player> duelParticipants = new HashMap<>(); // Mappa per tenere traccia dei partecipanti al duello
    private DuelWorldManager worldManager;

    public DuelManager(BetterDuels main, DuelWorldManager worldManager) {
        this.main = main;
        this.worldManager = worldManager;
        this.duelWorld =  worldManager.getDuelWorld();
        this.endDuelWorld = worldManager.getEndDuelWorld();

    }


    public void startDuel(Player player1, Player player2) {
        main.getServer().getPluginManager().registerEvents(new PlayerListener(this, main), main);
        // Salva gli inventari dei giocatori prima di iniziare il duello
        backupInventory(player1);
        backupInventory(player2);

        // Trasporta i giocatori nel mondo dedicato ai duelli
        teleportToDuelWorld(player1, player2);


        // Imposta il gioco dei giocatori a SURVIVAL per evitare il volo
        setGameMode(player1,player2);


        // Aggiungi i partecipanti al duello alla mappa dei partecipanti
        duelParticipants.put(player1, player2);
        duelParticipants.put(player2, player1);
    }

    public void endDuel(Player player1, Player player2) {
        // Ripristina gli inventari dei giocatori dopo il duello
        restoreInventory(player1,player2);


        // Trasporta i giocatori nel mondo principale
        teleportToMainWorld(player1,player2);


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


    // Metodi per il backup e il ripristino degli inventari
    private void backupInventory(Player player) {
        playerInventoryBackup.put(player, player.getInventory().getContents().clone());
    }

    private void restoreInventory(Player player1, Player player2) {
        ItemStack[] inventoryContents = playerInventoryBackup.remove(player1);
        if (inventoryContents != null) {
            player1.getInventory().setContents(inventoryContents);
            player1.updateInventory();
        }
    }


    private void teleportToDuelWorld(Player player1, Player player2) {
        // Utilizza le posizioni specificate da DuelWorldManager per teleportare i giocatori
        player1.teleport(worldManager.getPlayer1Location());
        player2.teleport(worldManager.getPlayer2Location());
    }

    private void teleportToMainWorld(Player player1, Player player2) {
        // Teleporta entrambi i giocatori alla posizione di fine duello specificata da DuelWorldManager

        player1.teleport( worldManager.endPlayerLocation());
        player2.teleport( worldManager.endPlayerLocation());
    }

    private void setGameMode(Player player1, Player player2) {
        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);
    }
}