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
    private final Map<Player, ItemStack[]> playerInventoryBackup1 = new HashMap<>();
    private final Map<Player, Player> duelParticipants = new HashMap<>();
    private DuelWorldManager worldManager;

    public DuelManager(BetterDuels main, DuelWorldManager worldManager) {
        this.main = main;
        this.worldManager = worldManager;

    }


    public void startDuel(Player player1, Player player2) {
        main.getServer().getPluginManager().registerEvents(new PlayerListener(this, main, worldManager), main);

        player2.setBedSpawnLocation(worldManager.endPlayerLocation(), true);

        backupInventory(player1, player2);

        teleportToDuelWorld(player1, player2);


        setGameMode(player1,player2);



        duelParticipants.put(player1, player2);
        duelParticipants.put(player2, player1);
    }

    public void endDuel(Player player1, Player player2) {

        restoreInventory(player1,player2);


        teleportToMainWorld(player1, player2);


        duelParticipants.remove(player1);
        duelParticipants.remove(player2);
    }


    public boolean isInDuel(Player player) {
        return duelParticipants.containsKey(player);
    }


    public Player getOtherPlayer(Player player) {
        return duelParticipants.get(player);
    }



    private void backupInventory(Player player1, Player player2) {
        playerInventoryBackup.put(player1, player1.getInventory().getContents().clone());
        playerInventoryBackup1.put(player2, player2.getInventory().getContents().clone());
    }

    private void restoreInventory(Player player1, Player player2) {
        ItemStack[] inventoryContents = playerInventoryBackup.remove(player1);
        if (inventoryContents != null) {
            player1.getInventory().setContents(inventoryContents);
            player1.updateInventory();
        }
        ItemStack[] inventoryContents1 = playerInventoryBackup1.remove(player2);
        if (inventoryContents != null) {
            player2.getInventory().setContents(inventoryContents1);
            player2.updateInventory();
        }
    }


    private void teleportToDuelWorld(Player player1, Player player2) {

        player1.teleport(worldManager.getPlayer1Location());
        player2.teleport(worldManager.getPlayer2Location());
    }

    private void teleportToMainWorld(Player player1, Player player2) {
        player1.teleport(worldManager.endPlayerLocation());
        player2.teleport(worldManager.endPlayerLocation());
    }


    private void setGameMode(Player player1, Player player2) {
        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);
    }
}