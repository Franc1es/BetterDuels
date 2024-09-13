package dev.francies.betterduels.database;

public class PlayerStat {
    private String playerUuid;
    private int wins;

    public PlayerStat(String playerUuid, int wins) {
        this.playerUuid = playerUuid;
        this.wins = wins;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public int getWins() {
        return wins;
    }
}
