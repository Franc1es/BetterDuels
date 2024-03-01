package dev.francies.betterduels.Database;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelStats implements CommandExecutor {
    private final BetterDuels plugin;
    private DatabaseConnection dbConnection;
    private File leaderboardFile;
    private FileConfiguration leaderboardConfig;
    private List<ArmorStand> leaderboardArmorStands = new ArrayList<>();
    private Location leaderboardLocation;

    public DuelStats(BetterDuels plugin, DatabaseConnection dbConnection) {
        this.plugin = plugin;
        this.dbConnection = dbConnection;
        createLeaderboardConfig();
        scheduleLeaderboardUpdates();
    }

    public void clearExistingLeaderboard() {
        World world = Bukkit.getWorld(leaderboardConfig.getString("location.world"));
        if (world == null) return;

        double x = leaderboardConfig.getDouble("location.x");
        double y = leaderboardConfig.getDouble("location.y");
        double z = leaderboardConfig.getDouble("location.z");
        Location loc = new Location(world, x, y, z);


        double radius = 5.0;


        List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(loc, radius, radius, radius);


            for (Entity entity : nearbyEntities) {
                if (entity instanceof ArmorStand && ((ArmorStand) entity).isMarker()) {
                    entity.remove();
                }
            }
    }

    private void createLeaderboardConfig() {
        leaderboardFile = new File(plugin.getDataFolder(), "leaderboard.yml");
        if (!leaderboardFile.exists()) {
            try {
                leaderboardFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        leaderboardConfig = YamlConfiguration.loadConfiguration(leaderboardFile);
        leaderboardLocation = loadLeaderboardLocation();
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
        as.setMarker(true);
        leaderboardArmorStands.add(as);
    }

    private void updateLeaderboard(Location loc) {
        removePreviousLeaderboard();
        if (loc == null) return;

        String header = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("leaderboard.header"));
        String entryFormat = plugin.getConfig().getString("leaderboard.entry_format");

        try {
            List<PlayerStat> topPlayers = dbConnection.getTopPlayersByWins(plugin.getConfig().getInt("leaderboard.player-to-show"));


            Location tempLoc = loc.clone();
            createFloatingText(tempLoc, header);

            tempLoc.add(0, -0.25, 0);

            for (int i = 0; i < topPlayers.size(); i++) {
                PlayerStat ps = topPlayers.get(i);
                String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(ps.getPlayerUuid())).getName();
                String message = entryFormat.replace("%rank%", Integer.toString(i + 1))
                        .replace("%player%", playerName)
                        .replace("%wins%", Integer.toString(ps.getWins()));

                tempLoc = loc.clone().add(0, -0.25 * (i + 1), 0);
                createFloatingText(tempLoc, ChatColor.translateAlternateColorCodes('&', message));
            }
        } catch (SQLException e) {
            plugin.getLogger().info(e.getLocalizedMessage());
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
        saveLeaderboardLocation(leaderboardLocation);
        updateLeaderboard(leaderboardLocation);

        return true;
    }

    private void saveLeaderboardLocation(Location loc) {
        leaderboardConfig.set("location.world", loc.getWorld().getName());
        leaderboardConfig.set("location.x", loc.getX());
        leaderboardConfig.set("location.y", loc.getY());
        leaderboardConfig.set("location.z", loc.getZ());
        try {
            leaderboardConfig.save(leaderboardFile);
        } catch (Exception e) {
            plugin.getLogger().info(e.getLocalizedMessage());
        }
    }

    public Location loadLeaderboardLocation() {
        if (leaderboardConfig.contains("location")) {
            World world = Bukkit.getWorld(leaderboardConfig.getString("location.world"));
            double x = leaderboardConfig.getDouble("location.x");
            double y = leaderboardConfig.getDouble("location.y");
            double z = leaderboardConfig.getDouble("location.z");
            return new Location(world, x, y, z);
        }
        return null;
    }
}
