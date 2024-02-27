package dev.francies.betterduels.PlayerStats;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Duels.DuelManager;
import dev.francies.betterduels.Mess.Messages;
import dev.francies.betterduels.WorldManager.DuelWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerListener implements Listener {
    private final DuelManager duelManager;
    private BetterDuels plugin;
    private DuelWorldManager worldManager;


    public PlayerListener(DuelManager duelManager, BetterDuels plugin, DuelWorldManager worldManager) {
        this.duelManager = duelManager;
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        Player defeatedplayer = event.getEntity();
        if (duelManager.isInDuel(defeatedplayer)) {
            Player otherPlayer = duelManager.getOtherPlayer(defeatedplayer);
            defeatedplayer.setBedSpawnLocation(worldManager.endPlayerLocation(), true);
            duelManager.endDuel(defeatedplayer, otherPlayer);
            otherPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("win")));
            defeatedplayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("defeat")));

        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (duelManager.isInDuel(player)) {
            Player otherPlayer = duelManager.getOtherPlayer(player);

            duelManager.endDuel(player, otherPlayer);

            otherPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("win-by-abbadon")));

            String commandTemplate = plugin.getConfig().getString("punishment-command");
            String command = commandTemplate.replace("%player%", player.getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
