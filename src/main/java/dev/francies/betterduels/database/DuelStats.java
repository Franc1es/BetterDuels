package dev.francies.betterduels.database;

import dev.francies.betterduels.BetterDuels;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class DuelStats extends PlaceholderExpansion {
    private final BetterDuels plugin;
    private DatabaseConnection dbConnection;

    public DuelStats(BetterDuels plugin, DatabaseConnection dbConnection) {
        this.plugin = plugin;
        this.dbConnection = dbConnection;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "duel";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        try {

            if (identifier.startsWith("player_")) {
                return getTopPlayerName(identifier);
            }


            if (identifier.startsWith("wins_")) {
                return getTopPlayerWins(identifier);
            }


            if (identifier.startsWith("total_")) {
                return getTopPlayerTotalDuels(identifier);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        } catch (NumberFormatException e) {
            return "Invalid placeholder format";
        }

        return null;
    }

    private String getTopPlayerName(String identifier) throws SQLException {
        int position = parsePosition(identifier, "player_");
        if (position < 0) {
            return "Invalid position";
        }

        Map<String, Integer> playerInfo = dbConnection.getPlayerInfoByRank(position);
        if (!playerInfo.isEmpty()) {
            String playerUuid = playerInfo.keySet().iterator().next();
            return Bukkit.getServer().getOfflinePlayer(UUID.fromString(playerUuid)).getName();
        } else {
            return "N/A";
        }
    }

    private String getTopPlayerWins(String identifier) throws SQLException {
        int position = parsePosition(identifier, "wins_");
        if (position < 0) {
            return "Invalid position";
        }

        Map<String, Integer> playerInfo = dbConnection.getPlayerInfoByRank(position);
        if (!playerInfo.isEmpty()) {
            return String.valueOf(playerInfo.values().iterator().next());
        } else {
            return "N/A";
        }
    }

    private String getTopPlayerTotalDuels(String identifier) throws SQLException {
        int position = parsePosition(identifier, "total_");
        if (position < 0) {
            return "Invalid position";
        }

        Map<String, Integer> playerInfo = dbConnection.getPlayerInfoByRank(position);
        if (!playerInfo.isEmpty()) {
            String playerUuid = playerInfo.keySet().iterator().next();
            return String.valueOf(dbConnection.getTotalDuelsForPlayer(playerUuid));
        } else {
            return "N/A";
        }
    }

    private int parsePosition(String identifier, String prefix) {
        try {
            return Integer.parseInt(identifier.substring(prefix.length())) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
