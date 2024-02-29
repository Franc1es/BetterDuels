package dev.francies.betterduels.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    private String host;
    private String databaseName;
    private String username;
    private String password;
    private boolean useSSL;

    public DatabaseConnection(String host, String databaseName, String username, String password, boolean useSSL) {
        this.host = host;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;

    }
    public List<PlayerStat> getTopPlayersByWins(int limit) throws SQLException {
        List<PlayerStat> topPlayers = new ArrayList<>();
        String query = "SELECT player_uuid, wins FROM BetterDuels_Stats ORDER BY wins DESC LIMIT ?";

        try (Connection conn = this.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String playerUuid = rs.getString("player_uuid");
                    int wins = rs.getInt("wins");
                    topPlayers.add(new PlayerStat(playerUuid, wins));
                }
            }
        }
        return topPlayers;
    }
    public void initialize() {

        String url = "jdbc:mysql://" + host + "/" + databaseName + "?useSSL="+ useSSL +"&autoReConnect=true";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {


            String sqlCreateDB = "CREATE DATABASE IF NOT EXISTS " + databaseName;
            stmt.executeUpdate(sqlCreateDB);


            try (Connection connDB = DriverManager.getConnection(url + "/" + databaseName, username, password)) {

                String sqlCreateTable = "CREATE TABLE IF NOT EXISTS BetterDuels_Stats (" +
                        "player_uuid VARCHAR(36) NOT NULL," +
                        "duels INT DEFAULT 0," +
                        "wins INT DEFAULT 0," +
                        "losses INT DEFAULT 0," +
                        "PRIMARY KEY (player_uuid)" +
                        ")";
                try (Statement stmtDB = connDB.createStatement()) {
                    stmtDB.executeUpdate(sqlCreateTable);
                }
            }

        } catch (SQLException e) {
            e.getLocalizedMessage();
            System.out.println(e);
        }
    }


    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + "/" + databaseName + "?useSSL="+ useSSL +"&autoReConnect=true";
        return DriverManager.getConnection(url, username, password);
    }
}
