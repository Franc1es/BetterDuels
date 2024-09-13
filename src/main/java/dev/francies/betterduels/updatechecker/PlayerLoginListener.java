package dev.francies.betterduels.updatechecker;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class PlayerLoginListener implements Listener {

    private final BetterDuels plugin;

    public PlayerLoginListener(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("betterduels.admin")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.checkForUpdates(player);
            }, 60L);
        }
    }
}
