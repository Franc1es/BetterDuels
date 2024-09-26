package dev.francies.betterduels.duels;

import dev.francies.betterduels.kits.InventoryClickListener;
import dev.francies.betterduels.mess.Messages;
import dev.francies.betterduels.playerstats.FreezePlayersListener;
import dev.francies.betterduels.worldmanager.DuelWorldManager;
import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.playerstats.PlayerListener;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;


import java.util.*;

public class DuelManager  {
    private final BetterDuels main;
    private final Map<Player, ItemStack[]> playerInventoryBackup = new HashMap<>();
    private final Map<Player, Player> duelParticipants = new HashMap<>();
    private final DuelWorldManager worldManager;
    private Map<Player, Boolean> kitSelectionMap = new HashMap<>();
    private final Map<UUID, Boolean> playersInCountdown = new HashMap<>();
    private final Map<Player, String> playerArenaMap = new HashMap<>();


    public DuelManager(BetterDuels main, DuelWorldManager worldManager) {
        this.main = main;
        this.worldManager = worldManager;

    }

    public void startDuel(Player player1, Player player2) {
        main.getServer().getPluginManager().registerEvents(new PlayerListener(this, main, worldManager, main.getDbConnection()), main);
        main.getServer().getPluginManager().registerEvents(new InventoryClickListener(main), main);
        main.getServer().getPluginManager().registerEvents(new FreezePlayersListener(main), main);


        backupInventory(player1);
        backupInventory(player2);
        clearAllPotionEffects(player1);
        clearAllPotionEffects(player2);


        duelParticipants.put(player1, player2);
        duelParticipants.put(player2, player1);
        kitSelectionMap.put(player1, false);
        kitSelectionMap.put(player2, false);


        main.getKitManager().openKitSelectionGUI(player1);
        main.getKitManager().openKitSelectionGUI(player2);


    }

    public void endDuel(Player player1, Player player2, String arenaName) {
        player1.getInventory().clear();
        player2.getInventory().clear();
        teleportToMainWorld(player1);


        Bukkit.getScheduler().runTaskLater(main, () -> {
            restoreInventory(player1);
            restoreInventory(player2);
        }, 40L);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            giveReward(player2);
        }, 40L);


        duelParticipants.remove(player1);
        duelParticipants.remove(player2);
        worldManager.releaseArena(arenaName);
        playerArenaMap.put(player1, arenaName);
        playerArenaMap.put(player2, arenaName);
    }



    private void giveReward(Player player) {

        String rewardCommand = main.getConfig().getString("reward.command");
        if (rewardCommand != null && !rewardCommand.isEmpty()) {
            rewardCommand = rewardCommand.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand);
        }
        int rewardAmount = main.getConfig().getInt("reward.amount");
        Material rewardMaterial = Material.valueOf(main.getConfig().getString("reward.material"));
        player.getInventory().addItem(new ItemStack(rewardMaterial, rewardAmount));
    }

    public String getArenaNameForPlayer(Player player) {
        return playerArenaMap.get(player);
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

    public void countDown(final Player player1, final Player player2) {
        playersInCountdown.put(player1.getUniqueId(), true);
        playersInCountdown.put(player2.getUniqueId(), true);
        String titleConfig = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("countdown.title"));
        String subtitleConfig = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("countdown.subtitle"));
        int countdownTime = main.getConfig().getInt("countdown.time-to-start", 5);

        for (int i = countdownTime; i > 0; i--) {
            int finalI = i;
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                String title = titleConfig.replace("%count%", String.valueOf(finalI));
                player1.sendTitle(title, subtitleConfig, 10, 20, 10);
                player2.sendTitle(title, subtitleConfig, 10, 20, 10);
            }, (countdownTime - finalI) * 20L);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            applySelectedKit(player1);
            applySelectedKit(player2);
            setGameMode(player1,player2);
            playersInCountdown.put(player1.getUniqueId(), false);
            playersInCountdown.put(player2.getUniqueId(), false);
        }, countdownTime * 20L);

    }


    private int teleportToDuelWorld(Player player1, Player player2) {
        DuelWorldManager.Arena arena = worldManager.allocateArena();
        if (arena == null) {
            player1.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.no-arena-free")));
            player2.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.no-arena-free")));
            noArenaFree(player1, player2);
            return 0;
        }else{
            player1.teleport(arena.getPlayer1Location());
            player2.teleport(arena.getPlayer2Location());
            return 1;
        }

    }

    public void teleportToMainWorld(Player player) {

        player.teleport(main.getWorldManager().getEndLocation());

    }

    public void setGameMode(Player player1, Player player2) {
        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);
    }

    public void clearAllPotionEffects(Player player) {
        for (PotionEffectType effect : PotionEffectType.values()) {
            if (player.hasPotionEffect(effect)) {
                player.removePotionEffect(effect);
            }
        }
    }
    public boolean isInCountdown(Player player) {
        return playersInCountdown.getOrDefault(player.getUniqueId(), false);
    }

    public void storeKitSelection(Player player, String kitName) {
        player.setMetadata("selectedKit", new FixedMetadataValue(main, kitName));
        markKitSelection(player, true);

        Player opponent = getOtherPlayer(player);
        if (hasSelectedKit(player) && hasSelectedKit(opponent)) {
            Bukkit.getScheduler().runTask(main, () -> {

                if (teleportToDuelWorld(player, opponent) == 1) {
                    countDown(player, opponent);
                    applySelectedKit(player);
                    applySelectedKit(opponent);
                    setGameMode(player, opponent);
                } else {
                    cancelDuel(player);
                    cancelDuel(opponent);
                }
            });
        }
    }

    public void applySelectedKit(Player player) {
        if (player.hasMetadata("selectedKit")) {
            List<MetadataValue> values = player.getMetadata("selectedKit");
            if (!values.isEmpty()) {
                String kitName = values.get(0).asString();
                if (main.getKitManager().kitExists(kitName)) {
                    main.getKitManager().giveKit(player, kitName);
                }
                player.removeMetadata("selectedKit", main);
            }
        }
    }
    public void cancelDuel(Player player) {
        Player opponent = getOtherPlayer(player);
        if (opponent != null) {

            String arenaName = playerArenaMap.get(player);

            restoreInventory(player);
            restoreInventory(opponent);

            duelParticipants.remove(player);
            duelParticipants.remove(opponent);
            playerArenaMap.remove(player);
            playerArenaMap.remove(opponent);
     if (arenaName != null) {
                worldManager.releaseArena(arenaName);
            }
            opponent.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.kit-not-selected").replace("%player%", player.getName())));
            opponent.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.duel-cancelled")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.duel-cancelled")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.remember-select-kit")));
        }
    }

    public void noArenaFree(Player player, Player opponent){
        String arenaName = playerArenaMap.get(player);
        worldManager.releaseArena(arenaName);
        restoreInventory(player);
        restoreInventory(opponent);
        duelParticipants.remove(player);
        duelParticipants.remove(opponent);
        playerArenaMap.remove(player);
        playerArenaMap.remove(opponent);
    }

    public void markKitSelection(Player player, boolean selected) {
        kitSelectionMap.put(player, selected);

        Player opponent = getOtherPlayer(player);
        if (kitSelectionMap.getOrDefault(player, false) && kitSelectionMap.getOrDefault(opponent, false)) {

            Bukkit.getScheduler().runTaskLater(main, () -> {
                clearAllPotionEffects(player);
                clearAllPotionEffects(opponent);

            }, 20L);
        }
    }


}