package dev.francies.betterduels.worldmanager;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DuelWorldManager {
    private final BetterDuels plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Queue<String> availableArenas = new LinkedList<>();

    public DuelWorldManager(BetterDuels plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    private void loadArenas() {
        ConfigurationSection arenasSection = plugin.getConfig().getConfigurationSection("arenas");
        if (arenasSection != null) {
            for (String key : arenasSection.getKeys(false)) {
                ConfigurationSection arenaSection = arenasSection.getConfigurationSection(key);
                Location player1Location = loadLocation(arenaSection.getConfigurationSection("player1_location"));
                Location player2Location = loadLocation(arenaSection.getConfigurationSection("player2_location"));
                arenas.put(key, new Arena(player1Location, player2Location));
                availableArenas.offer(key);
            }
        }
    }
    public Location getEndLocation(){
        double playerendX = plugin.getConfig().getDouble("players_end_location.x");
        double playerendY = plugin.getConfig().getDouble("players_end_location.y");
        double playerendZ = plugin.getConfig().getDouble("players_end_location.z");
        String endDuelWorldName = plugin.getConfig().getString("world_endDuel");
        World endDuelWorld = Bukkit.getWorld(endDuelWorldName);
        return new Location(endDuelWorld, playerendX, playerendY, playerendZ);
    }
    private Location loadLocation(ConfigurationSection section) {
        return new Location(Bukkit.getWorld(plugin.getConfig().getString("world_duel")),
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch"));
    }

    public Arena allocateArena() {
        String arenaName = availableArenas.poll();
        return arenaName == null ? null : arenas.get(arenaName);
    }

    public void releaseArena(String arenaName) {
        availableArenas.offer(arenaName);
    }

    public static class Arena {
        private final Location player1Location;
        private final Location player2Location;

        public Arena(Location player1Location, Location player2Location) {
            this.player1Location = player1Location;
            this.player2Location = player2Location;
        }

        public Location getPlayer1Location() {
            return player1Location;
        }

        public Location getPlayer2Location() {
            return player2Location;
        }
    }
}
