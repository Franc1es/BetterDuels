package dev.francies.betterduels.WorldManager;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class DuelWorldManager {
    private final BetterDuels main;
    private World duelWorld;
    private Location player1Location;
    private Location player2Location;
    private Location playersend;
    private World endDuelWorld;
    public DuelWorldManager(BetterDuels main) {
        this.main = main;
        loadWorldAndLocations();
    }

    private void loadWorldAndLocations() {

        main.reloadConfig();

        String duelWorldName = main.getConfig().getString("world_duel");
        String endDuelWorldName = main.getConfig().getString("world_endDuel");

        double player1X = main.getConfig().getDouble("player1_location.x");
        double player1Y = main.getConfig().getDouble("player1_location.y");
        double player1Z = main.getConfig().getDouble("player1_location.z");
        double player2X = main.getConfig().getDouble("player2_location.x");
        double player2Y = main.getConfig().getDouble("player2_location.y");
        double player2Z = main.getConfig().getDouble("player2_location.z");
        double playerendX = main.getConfig().getDouble("players_end_location.x");
        double playerendY = main.getConfig().getDouble("players_end_location.y");
        double playerendZ = main.getConfig().getDouble("players_end_location.z");
        double pitch1 = main.getConfig().getDouble("player1_location.pitch");
        double pitch2 = main.getConfig().getDouble("player2_location.pitch");
        double yaw1 = main.getConfig().getDouble("player1_location.yaw");
        double yaw2 = main.getConfig().getDouble("player2_location.yaw");

        duelWorld = Bukkit.getWorld(duelWorldName);
        if (duelWorld == null) {
            main.getLogger().warning("Impossibile trovare il mondo per l'inizio del duello.");
            return;
        }

        endDuelWorld = Bukkit.getWorld(endDuelWorldName);
        if (endDuelWorld == null) {
            main.getLogger().warning("Impossibile trovare il mondo per la fine del duello.");
            return;
        }


        player1Location = new Location(duelWorld, player1X, player1Y, player1Z, (float) yaw1, (float) pitch1);
        player2Location = new Location(duelWorld, player2X, player2Y, player2Z, (float) yaw2, (float) pitch2);
        playersend = new Location(endDuelWorld, playerendX, playerendY, playerendZ);
    }

    public World getEndDuelWorld(){
        return endDuelWorld;
    }
    public Location getPlayer1Location() {
        return player1Location;
    }
    public Location endPlayerLocation() {
        return playersend;
    }
    public Location getPlayer2Location() {
        return player2Location;
    }
}
