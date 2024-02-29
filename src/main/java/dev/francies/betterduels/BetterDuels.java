package dev.francies.betterduels;

import dev.francies.betterduels.Admin.ReloadCommand;
import dev.francies.betterduels.Database.DatabaseConnection;
import dev.francies.betterduels.Database.DuelStats;
import dev.francies.betterduels.Duels.DuelCommand;
import dev.francies.betterduels.Duels.DuelManager;
import dev.francies.betterduels.Kits.KitManager;
import dev.francies.betterduels.Mess.Messages;
import dev.francies.betterduels.WorldManager.DuelWorldManager;

import org.bukkit.plugin.java.JavaPlugin;


public final class BetterDuels extends JavaPlugin {
    private KitManager kitManager;
    private DuelManager duelManager;
    private DuelWorldManager worldManager;
    private DatabaseConnection dbConnection;

    @Override
    public void onEnable() {
        Messages.loadMessages(this);
        kitManager = new KitManager(this);
        worldManager = new DuelWorldManager(this);
        duelManager = new DuelManager(this, worldManager);

        this.saveDefaultConfig();

        DuelCommand duelCommandExecutor = new DuelCommand(this);

        getCommand("duel").setExecutor(duelCommandExecutor);
        getCommand("duelaccept").setExecutor(duelCommandExecutor);
        getCommand("dueldeny").setExecutor(duelCommandExecutor);
        this.getCommand("btreload").setExecutor(new ReloadCommand(this));



        String host = getConfig().getString("database.host");
        String databaseName = getConfig().getString("database.databasename");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");
        boolean useSSL = getConfig().getBoolean("database.flagssl");
        dbConnection = new DatabaseConnection(host, databaseName, username, password, useSSL);
        dbConnection.initialize();
        this.getCommand("duelstats").setExecutor(new DuelStats(this, dbConnection));

        getLogger().info("BETTERDUELS abilitato! by Francies");
    }

    @Override
    public void onDisable() {
        getLogger().info("BETTERDUELS disabilitato! by Francies");
    }

    public KitManager getKitManager() {
        return kitManager;
    }


    public DuelManager getDuelManager() {
        return duelManager;
    }


    public DatabaseConnection getDbConnection() {
        return dbConnection;
    }
}
