package dev.francies.betterduels;

import com.google.gson.JsonObject;
import dev.francies.betterduels.admin.DuelStatsRemove;
import dev.francies.betterduels.admin.ReloadCommand;
import dev.francies.betterduels.database.DatabaseConnection;
import dev.francies.betterduels.database.DuelStats;
import dev.francies.betterduels.duels.DuelCommand;
import dev.francies.betterduels.duels.DuelManager;
import com.google.gson.JsonParser;
import dev.francies.betterduels.kits.KitManager;
import dev.francies.betterduels.mess.Messages;
import dev.francies.betterduels.playerstats.onInventoryCloseEvent;
import dev.francies.betterduels.updatechecker.PlayerLoginListener;
import dev.francies.betterduels.worldmanager.DuelWorldManager;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;


public final class BetterDuels extends JavaPlugin {
    private KitManager kitManager;
    private DuelManager duelManager;
    private DuelWorldManager worldManager;
    private DatabaseConnection dbConnection;
    private File leaderboardFile;
    private FileConfiguration leaderboardConfig;
    private final String versionUrl = "https://www.franciesdev.it/api/betterduels.json";

    @Override
    public void onEnable() {

        int pluginId = 23353;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new SingleLineChart("players", () -> Bukkit.getOnlinePlayers().size()));

        long startTime = System.currentTimeMillis();

        getLogger().log(Level.INFO, "_________________________");
        getLogger().log(Level.INFO, "BetterDuels v" + this.getDescription().getVersion());
        getLogger().log(Level.INFO, "Author: " + this.getDescription().getAuthors().get(0));
        getLogger().log(Level.INFO, "_________________________");
        getLogger().log(Level.INFO, "");
        Messages.loadMessages(this);

        kitManager = new KitManager(this);
        worldManager = new DuelWorldManager(this);
        duelManager = new DuelManager(this, worldManager);

        this.saveDefaultConfig();

        String host = getConfig().getString("database.host");
        String databaseName = getConfig().getString("database.databasename");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");
        boolean useSSL = getConfig().getBoolean("database.flagssl");
        dbConnection = new DatabaseConnection(host, databaseName, username, password, useSSL);
        dbConnection.initialize();

        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);

        DuelStats duelStats = new DuelStats(this, dbConnection);
        duelStats.clearExistingLeaderboard();

        this.getServer().getPluginManager().registerEvents(new onInventoryCloseEvent(this), this);
        DuelCommand duelCommandExecutor = new DuelCommand(this);
        getCommand("duel").setExecutor(duelCommandExecutor);
        getCommand("duelaccept").setExecutor(duelCommandExecutor);
        getCommand("dueldeny").setExecutor(duelCommandExecutor);
        this.getCommand("btreload").setExecutor(new ReloadCommand(this));
        this.getCommand("duelstats").setExecutor(duelStats);
        this.getCommand("duelstatsremove").setExecutor(new DuelStatsRemove(this));
        getLogger().log(Level.INFO, "_________________________");
        getLogger().log(Level.INFO, "Loaded in " + (System.currentTimeMillis() - startTime) + "ms");
        getLogger().log(Level.INFO, "_________________________");
    }


    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, "_________________________");
        this.getLogger().log(Level.INFO, "BetterDuels v" + this.getDescription().getVersion() + " says bye bye");
        this.getLogger().log(Level.INFO, "_________________________");
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public DuelWorldManager getWorldManager(){
        return worldManager;
    }


    public DuelManager getDuelManager() {
        return duelManager;
    }


    public DatabaseConnection getDbConnection() {
        return dbConnection;
    }


    private void createLeaderboardConfig() {
        leaderboardFile = new File(getDataFolder(), "leaderboard.yml");
        if (!leaderboardFile.exists()) {
            try {
                leaderboardFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        leaderboardConfig = YamlConfiguration.loadConfiguration(leaderboardFile);
    }

    public FileConfiguration getLeaderboardConfig() {
        if (leaderboardConfig == null) {
            createLeaderboardConfig();
        }
        return leaderboardConfig;
    }

    public void saveLeaderboardConfig() {
        try {
            leaderboardConfig.save(leaderboardFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void checkForUpdates(Player player) {

        try {

            URL url = new URL(versionUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();


            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(content.toString()).getAsJsonObject();


            String latestVersion = json.get("version").getAsString();
            String downloadUrl1 = json.get("downloadUrl1").getAsString();
            String downloadUrl2 = json.get("downloadUrl2").getAsString();


            String currentVersion = this.getDescription().getVersion();

            if (!currentVersion.equals(latestVersion)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.prefix") +" &eA newer version is available: &f" + latestVersion));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.prefix") +"&bDownload link 1: &f" + downloadUrl1));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.prefix") +"&bDownload link 2: &f" + downloadUrl2));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
