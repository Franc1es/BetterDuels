package dev.francies.betterduels.database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnection {

    private String host;
    private String databaseName;
    private String username;
    private String password;
    private boolean useSSL;
    private Connection connection;

    public DatabaseConnection(String host, String databaseName, String username, String password, boolean useSSL) {
        this.host = host;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
    }

    public void initialize() {
        try {
            openConnection();
            createTable();
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getLocalizedMessage());
        }
    }
    public Connection getConnection() throws SQLException {
        checkConnection();
        return this.connection;
    }

    private void openConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return;
        }

        String url = "jdbc:mysql://" + host + "/" + databaseName + "?useSSL=" + useSSL + "&autoReconnect=true";
        this.connection = DriverManager.getConnection(url, username, password);
    }

    private void checkConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed() || !this.connection.isValid(2)) {
            openConnection();
        }
    }

    private void createTable() throws SQLException {
        checkConnection();

        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS BetterDuels_Stats (" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "duels INT DEFAULT 0," +
                "wins INT DEFAULT 0," +
                "losses INT DEFAULT 0," +
                "PRIMARY KEY (player_uuid)" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sqlCreateTable);
        }
    }

    public Map<String, Integer> getPlayerInfoByRank(int position) throws SQLException {
        checkConnection();

        Map<String, Integer> playerInfo = new HashMap<>();
        String query = "SELECT player_uuid, wins FROM BetterDuels_Stats ORDER BY wins DESC LIMIT 1 OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, position);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    playerInfo.put(rs.getString("player_uuid"), rs.getInt("wins"));
                }
            }
        }
        return playerInfo;
    }

    public int getTotalDuelsForPlayer(String playerUuid) throws SQLException {
        checkConnection();

        String query = "SELECT duels FROM BetterDuels_Stats WHERE player_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerUuid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("duels");
                }
            }
        }
        return 0;
    }

}
