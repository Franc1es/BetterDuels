package dev.francies.betterduels.Duels;

import dev.francies.betterduels.Kits.InventoryClickListener;
import dev.francies.betterduels.Mess.Messages;
import dev.francies.betterduels.WorldManager.DuelWorldManager;
import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.PlayerStats.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class DuelManager  {
    private final BetterDuels main;
    private final Map<Player, ItemStack[]> playerInventoryBackup = new HashMap<>();
    private final Map<Player, Player> duelParticipants = new HashMap<>();
    private final DuelWorldManager worldManager;
    private Map<Player, Boolean> kitSelectionMap = new HashMap<>();

    public DuelManager(BetterDuels main, DuelWorldManager worldManager) {
        this.main = main;
        this.worldManager = worldManager;
    }

    public void startDuel(Player player1, Player player2) {
        main.getServer().getPluginManager().registerEvents(new PlayerListener(this, main, worldManager), main);
        main.getServer().getPluginManager().registerEvents(new InventoryClickListener(main), main);

        backupInventory(player1);
        backupInventory(player2);

        clearAllPotionEffects(player1);
        clearAllPotionEffects(player2);

        teleportToDuelWorld(player1,player2);



        duelParticipants.put(player1, player2);
        duelParticipants.put(player2, player1);


        main.getKitManager().openKitSelectionGUI(player1);
        main.getKitManager().openKitSelectionGUI(player2);

    }

    public void endDuel(Player player1, Player player2) {
        player1.getInventory().clear();
        player2.getInventory().clear();
        teleportToMainWorld(player1, player2);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            restoreInventory(player1);
            restoreInventory(player2);
        }, 40L);

        duelParticipants.remove(player1);
        duelParticipants.remove(player2);
    }


    public boolean isInDuel(Player player) {
        return duelParticipants.containsKey(player);
    }

    public Player getOtherPlayer(Player player) {
        return duelParticipants.get(player);
    }
    public boolean hasSelectedKit(Player player) {
        return kitSelectionMap.getOrDefault(player, false);
    }
    private void backupInventory(Player player) {
        playerInventoryBackup.put(player, player.getInventory().getContents().clone());
        player.getInventory().clear();
    }

    public void restoreInventory(Player player) {
        ItemStack[] inventoryContents = playerInventoryBackup.remove(player);
        if (inventoryContents != null) {
            player.getInventory().setContents(inventoryContents);
            player.updateInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("inventory-restore")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("inventory-restore-empty")));
        }
    }

    public void kitSelected(Player player) {

        Player enemy = getOtherPlayer(player);

            String titleConfig = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("countdown.title"));
            String subtitleConfig = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("countdown.subtitle"));

            for (int i = main.getConfig().getInt("countdown.time-to-start", 5); i > 0; i--) {
                int finalI = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

                    String title = titleConfig.replace("%count%", String.valueOf(finalI));
                    player.sendTitle(title, subtitleConfig, 10, 20, 10);
                    enemy.sendTitle(title, subtitleConfig, 10, 20, 10);
                }, (main.getConfig().getInt("countdown.time-to-start",5) - finalI) * 20L);
            }


            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

            }, 5 * 20L);

    }

    private void teleportToDuelWorld(Player player1, Player player2) {
        player1.teleport(worldManager.getPlayer1Location());
        player2.teleport(worldManager.getPlayer2Location());
    }

    private void teleportToMainWorld(Player player1, Player player2) {
        player1.teleport(worldManager.endPlayerLocation());
        player2.teleport(worldManager.endPlayerLocation());
    }

    public void setGameMode(Player player1, Player player2) {
        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);
    }
    public void setGameMode1(Player player1, Player player2) {
        player1.setGameMode(GameMode.ADVENTURE);
        player2.setGameMode(GameMode.ADVENTURE);
    }
    public void clearAllPotionEffects(Player player) {
        for (PotionEffectType effect : PotionEffectType.values()) {
            if (player.hasPotionEffect(effect)) {
                player.removePotionEffect(effect);
            }
        }
    }
    public void markKitSelection(Player player, boolean selected) {
        kitSelectionMap.put(player, selected);

        Player opponent = getOtherPlayer(player);
        if (opponent != null && kitSelectionMap.getOrDefault(player, false) && kitSelectionMap.getOrDefault(opponent, false)) {

            kitSelected(player);
            kitSelected(opponent);
        }
    }

}
