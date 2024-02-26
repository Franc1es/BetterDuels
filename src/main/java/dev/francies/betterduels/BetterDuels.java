package dev.francies.betterduels;

import dev.francies.betterduels.Duels.*;
import dev.francies.betterduels.PlayerStats.*;
import dev.francies.betterduels.WorldManager.*;
import dev.francies.betterduels.Kits.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterDuels extends JavaPlugin {

    private DuelManager manager;
    private KitManager kitManager;


    @Override
    public void onEnable() {


        kitManager = new KitManager(this);

        // Caricamento config
        this.saveDefaultConfig();

        // Comandi del duel
        this.getCommand("duel").setExecutor(new DuelCommand(this));
        this.getCommand("kit").setExecutor(new KitCommand(this));

        // Listener

        getLogger().info("BETTERDUELS abilitato! by Francies");

    }

    @Override
    public void onDisable() {

        // Salvataggio dei dati
       // playerStatsManager.savePlayerStats();
        getLogger().info("BETTERDUELS disabilitato! by Francies");
    }

    public KitManager getKitManager() {
        return kitManager;
    }


}
