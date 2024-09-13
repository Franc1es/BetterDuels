package dev.francies.betterduels.duels;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelCommand implements CommandExecutor {
    private final BetterDuels plugin;
    private Map<UUID, Player> pendingRequests = new ConcurrentHashMap<>();

    public DuelCommand(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm")));
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("duel")) {
            return handleDuel(player, args);
        } else if (label.equalsIgnoreCase("duelaccept")) {
            return handleDuelAccept(player, args);
        } else if (label.equalsIgnoreCase("dueldeny")) {
            return handleDuelDeny(player, args);
        } else {
            player.sendMessage(ChatColor.RED + "Command not found");
            return true;
        }
    }

    private boolean handleDuel(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-usage-command")));
            return true;
        }
        if (!player.hasPermission("betterduels.start")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm")));
            return true;
        }


        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-not-online").replace("%player%", targetPlayer.getName())));
            return true;
        }
        if (!targetPlayer.hasPermission("betterduels.start")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm-target").replace("%player%", targetPlayer.getName())));
            return true;
        }
        if( player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-creative-spectator")));
            return true;
        }
        if( targetPlayer.getGameMode() == GameMode.CREATIVE || targetPlayer.getGameMode() == GameMode.SPECTATOR){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-creative-spectator-target").replace("%player%", targetPlayer.getName())));
            return true;
        }
        if (Objects.equals(targetPlayer.getName(), player.getName())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.error-duel-youself")));
            return true;
        }
        if (hasPendingRequest(targetPlayer)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.already-in-request").replace("%player%", targetPlayer.getName())));
            return true;
        }
        if (plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-duel-command-during-duel").replace("%player%", targetPlayer.getName())));
            return true;
        }
        if (plugin.getDuelManager().isInDuel(targetPlayer)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.already-in-a-duel").replace("%player%", targetPlayer.getName())));
            return true;
        }

        pendingRequests.put(targetPlayer.getUniqueId(), player);
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-received").replace("%player%", player.getName())));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-sent").replace("%player%", targetPlayer.getName())));

        UUID targetPlayerId = targetPlayer.getUniqueId();


        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(targetPlayerId)) {
                pendingRequests.remove(targetPlayerId);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-expired-target").replace("%player%", targetPlayer.getName())));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-expired").replace("%player%", player.getName())));
            }
        }, plugin.getConfig().getInt("duel.time-to-life")*20L);

        return true;
    }

    private boolean handleDuelAccept(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-accept-usage-command")));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }
        if (!targetPlayer.hasPermission("betterduels.accept")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm-target")));
            return true;
        }
        if (hasPendingRequest(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-accepted-target").replace("%player%", targetPlayer.getName())));
            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-accepted").replace("%player%", player.getName())));
            plugin.getDuelManager().startDuel(player, targetPlayer);
            removePendingRequest(player);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-challenge")));
        }

        return true;
    }

    private boolean handleDuelDeny(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-deny-usage-command")));
            return true;
        }

        Player targetPlayer = plugin.getServer().getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }
        if (!targetPlayer.hasPermission("betterduels.deny")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }
        if (hasPendingRequest(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-denied-target").replace("%player%", targetPlayer.getName())));
            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-request-denied").replace("%player%", player.getName())));
            removePendingRequest(player);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-challenge")));
        }

        return true;
    }

    public boolean hasPendingRequest(Player player) {
        return pendingRequests.containsKey(player.getUniqueId());
    }

    public void removePendingRequest(Player player) {
        pendingRequests.remove(player.getUniqueId());
    }

}
