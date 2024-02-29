package dev.francies.betterduels.Database;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelStats implements CommandExecutor {
    private final BetterDuels plugin;
    private DatabaseConnection dbConnection;
    private List<ArmorStand> leaderboardArmorStands = new ArrayList<>();
    private Location leaderboardLocation;

    public DuelStats(BetterDuels plugin, DatabaseConnection dbConnection) {
        this.plugin = plugin;
        this.dbConnection = dbConnection;
        scheduleLeaderboardUpdates();
    }

    private void removePreviousLeaderboard() {
        for (ArmorStand as : leaderboardArmorStands) {
            as.remove();
        }
        leaderboardArmorStands.clear();
    }

    private void createFloatingText(Location loc, String text) {
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);
        as.setGravity(false);
        as.setCanPickupItems(false);
        as.setCustomName(ChatColor.translateAlternateColorCodes('&', text));
        as.setCustomNameVisible(true);
        as.setVisible(false);
        leaderboardArmorStands.add(as);
    }

    private void updateLeaderboard(Location loc) {


        removePreviousLeaderboard();
        String header = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("leaderboard.header"));
        String entryFormat = plugin.getConfig().getString("leaderboard.entry_format");

        try {
            List<PlayerStat> topPlayers = dbConnection.getTopPlayersByWins(plugin.getConfig().getInt("leaderboard.player-to-show"));

            createFloatingText(loc, header);
            loc.add(0, -0.25, 0);

            for (int i = 0; i < topPlayers.size(); i++) {
                PlayerStat ps = topPlayers.get(i);
                String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(ps.getPlayerUuid())).getName();
                String message = entryFormat.replace("%rank%", Integer.toString(i + 1))
                        .replace("%player%", playerName)
                        .replace("%wins%", Integer.toString(ps.getWins()));
                createFloatingText(loc, ChatColor.translateAlternateColorCodes('&', message));
                loc.add(0, -0.25, 0);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Si è verificato un errore durante l'aggiornamento della leaderboard: " + e.getMessage());
        }
    }

    public void scheduleLeaderboardUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (leaderboardLocation != null) {
                    updateLeaderboard(leaderboardLocation);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * plugin.getConfig().getInt("leaderboard.stats-update"));
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm")));
            return true;
        }
        if (!sender.hasPermission("betterduels.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm")));
            return true;
        }

        Player player = (Player) sender;

        leaderboardLocation = player.getLocation().clone().add(0, 2, 0);
        updateLeaderboard(leaderboardLocation);

        return true;
    }

}
