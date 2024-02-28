package dev.francies.betterduels.Duels;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.Queue;

public class DuelCommand implements CommandExecutor {
    private final BetterDuels plugin;
    private final Queue<DuelRequest> duelRequests = new LinkedList<>();

    public DuelCommand(BetterDuels plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm"));
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("duel")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.duel-usage-command"));
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                player.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player-not-online"));
                return true;
            }

            if (player.equals(targetPlayer)) {
                player.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.error-duel-youself"));
                return true;
            }

            // Memorizza la richiesta di duello
            BukkitTask expiryTask = new BukkitRunnable() {
                @Override
                public void run() {
                    DuelRequest request = getDuelRequest(player);
                    if (request != null) {
                        duelRequests.remove(request);
                        player.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-challenge"));
                        targetPlayer.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-challenge"));
                    }
                }
            }.runTaskLater(plugin, plugin.getConfig().getInt("duel.time-to-life", 60) * 20);

            DuelRequest request = new DuelRequest(player, targetPlayer, expiryTask);
            duelRequests.offer(request);
            plugin.getDuelManager().sendDuelRequest(player, targetPlayer);

            return true;
        } else if (label.equalsIgnoreCase("duelaccept")) {
            // Accetta la richiesta di duello
            DuelRequest request = getDuelRequest(player);
            if (request != null) {
                duelRequests.remove(request);
                plugin.getDuelManager().acceptDuelRequest(request);
            } else {
                player.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-challenge"));
            }
            return true;
        } else if (label.equalsIgnoreCase("dueldeny")) {
            // Rifiuta la richiesta di duello
            DuelRequest request = getDuelRequest(player);
            if (request != null) {
                duelRequests.remove(request);
                plugin.getDuelManager().denyDuelRequest(player);
            } else {
                player.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-challenge"));
            }
            return true;
        }

        return false;
    }

    private DuelRequest getDuelRequest(Player player) {
        for (DuelRequest request : duelRequests) {
            if (request.getTargetPlayer().equals(player) || request.getRequester().equals(player)) {
                return request;
            }
        }
        return null;
    }
}
