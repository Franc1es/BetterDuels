package dev.francies.betterduels.Duels;

import dev.francies.betterduels.Kits.InventoryClickListener;
import dev.francies.betterduels.Mess.Messages;
import dev.francies.betterduels.PlayerStats.FreezePlayersListener;
import dev.francies.betterduels.WorldManager.DuelWorldManager;
import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.PlayerStats.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DuelManager  {
    private final BetterDuels main;
    private final Map<Player, ItemStack[]> playerInventoryBackup = new HashMap<>();
    private final Map<Player, Player> duelParticipants = new HashMap<>();
    private final DuelWorldManager worldManager;
    private Map<Player, Boolean> kitSelectionMap = new HashMap<>();
    private final Map<UUID, Boolean> playersInCountdown = new HashMap<>();
    private Queue<DuelRequest> duelRequests = new LinkedList<>();
    private BukkitTask expiryTask;

    public DuelManager(BetterDuels main, DuelWorldManager worldManager) {
        this.main = main;
        this.worldManager = worldManager;

    }

    public void startDuel(Player player1, Player player2) {
        main.getServer().getPluginManager().registerEvents(new PlayerListener(this, main, worldManager), main);
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

    public void endDuel(Player player1, Player player2) {
        player1.getInventory().clear();
        player2.getInventory().clear();
        teleportToMainWorld(player1, player2);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            restoreInventory(player1);
            restoreInventory(player2);
        }, 40L);

        giveReward(player2);

        duelParticipants.remove(player1);
        duelParticipants.remove(player2);
    }
    public void acceptDuelRequest(DuelRequest request) {
        Player challenger = request.getRequester();
        Player targetPlayer = request.getTargetPlayer();

        // Invia messaggi di conferma ai giocatori
        challenger.sendMessage(ChatColor.GREEN + main.getConfig().getString("messages.prefix") + "Your duel request to " + targetPlayer.getName() + " has been accepted.");
        targetPlayer.sendMessage(ChatColor.GREEN + main.getConfig().getString("messages.prefix") + "You have accepted the duel request from " + challenger.getName() + ".");

        // Implementa ulteriori logiche per avviare il duello
        startDuel(challenger, targetPlayer);
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


    public boolean isInDuel(Player player) {
        return duelParticipants.containsKey(player);
    }
    public void denyDuelRequest(Player targetPlayer) {
        // Rimuovi la richiesta di duello per il giocatore specificato dalla coda
        DuelRequest requestToRemove = null;
        for (DuelRequest request : duelRequests) {
            if (request.getTargetPlayer().equals(targetPlayer)) {
                requestToRemove = request;
                break;
            }
        }
        if (requestToRemove != null) {
            duelRequests.remove(requestToRemove);

            // Invia un messaggio al giocatore che ha inviato la richiesta
            Player requester = requestToRemove.getRequester();
            requester.sendMessage(ChatColor.RED + main.getConfig().getString("messages.prefix") + "Your duel request to " + targetPlayer.getName() + " has been denied.");
        } else {
            // Se non c'è nessuna richiesta di duello per il giocatore specificato
            targetPlayer.sendMessage(ChatColor.RED + main.getConfig().getString("messages.prefix") + "There are no pending duel requests for you.");
        }
    }

    public Player getOtherPlayer(Player player) {
        return duelParticipants.get(player);
    }
    public boolean hasSelectedKit(Player player) {
        return kitSelectionMap.getOrDefault(player, false);
    }
    public void sendDuelRequest(Player requester, Player targetPlayer) {
        // Crea una nuova istanza di DuelRequest
        DuelRequest request = new DuelRequest(requester, targetPlayer, expiryTask);

        // Aggiungi la richiesta alla coda delle richieste di duello
        duelRequests.offer(request);

        // Invia messaggi di conferma ai giocatori
        requester.sendMessage(ChatColor.GREEN + main.getConfig().getString("messages.prefix") + "You have sent a duel request to " + targetPlayer.getName() + ".");
        targetPlayer.sendMessage(ChatColor.YELLOW + main.getConfig().getString("messages.prefix") + "You have received a duel request from " + requester.getName() + ". Type /duelaccept or /dueldeny to respond.");
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

                teleportToDuelWorld(player, opponent);
                countDown(player, opponent);
                applySelectedKit(player);
                applySelectedKit(opponent);

                setGameMode(player, opponent);
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
