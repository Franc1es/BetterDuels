package dev.francies.betterduels.playerstats;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.database.DatabaseConnection;
import dev.francies.betterduels.duels.DuelManager;
import dev.francies.betterduels.mess.Messages;
import dev.francies.betterduels.worldmanager.DuelWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;


public class PlayerListener implements Listener {
    private final DuelManager duelManager;
    private BetterDuels plugin;
    private DuelWorldManager worldManager;
    private final DatabaseConnection dbConnection;


    public PlayerListener(DuelManager duelManager, BetterDuels plugin, DuelWorldManager worldManager, DatabaseConnection dbConnection) {
        this.duelManager = duelManager;
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.dbConnection = dbConnection;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        event.getDrops().clear();
        Player defeatedPlayer = event.getEntity();
        if (duelManager.isInDuel(defeatedPlayer)) {
            Player otherPlayer = duelManager.getOtherPlayer(defeatedPlayer);
            defeatedPlayer.setBedSpawnLocation(worldManager.getEndLocation(), true);
            duelManager.endDuel(defeatedPlayer, otherPlayer, duelManager.getArenaNameForPlayer(defeatedPlayer));
            otherPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("win")));
            defeatedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("defeat")));


            try {
                updatePlayerStats(defeatedPlayer.getUniqueId(), false);
                updatePlayerStats(otherPlayer.getUniqueId(), true);
            } catch (SQLException e) {
                plugin.getLogger().info(e.toString());
            }
        }
    }

    private void updatePlayerStats(UUID playerUuid, boolean won) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            String query = "INSERT INTO BetterDuels_Stats (player_uuid, duels, wins, losses) VALUES (?, 1, ?, ?) ON DUPLICATE KEY UPDATE duels = duels + 1, wins = wins + ?, losses = losses + ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, playerUuid.toString());
                ps.setInt(2, won ? 1 : 0);
                ps.setInt(3, won ? 0 : 1);
                ps.setInt(4, won ? 1 : 0);
                ps.setInt(5, won ? 0 : 1);
                ps.executeUpdate();
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (duelManager.isInDuel(player)) {
            Player otherPlayer = duelManager.getOtherPlayer(player);

            duelManager.endDuel(player, otherPlayer, duelManager.getArenaNameForPlayer(player));

            otherPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("win-by-abbadon")));

            String commandTemplate = plugin.getConfig().getString("punishment-command");
            String command = commandTemplate.replace("%player%", player.getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
