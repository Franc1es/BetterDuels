package dev.francies.betterduels.PlayerStats;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;


public class FreezePlayersListener implements Listener {
    private final BetterDuels plugin;

    public FreezePlayersListener(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();


        if (plugin.getDuelManager().isInCountdown(player)) {

            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.getDuelManager().isInCountdown(player)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onCrossbowShoot(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            if (plugin.getDuelManager().isInCountdown(player)) {
                event.setCancelled(true);

            }
    }

    @EventHandler
    public void onTridentThrow(ProjectileLaunchEvent event) {
            Player player = (Player) event.getEntity().getShooter();
            if (plugin.getDuelManager().isInCountdown(player)) {
                event.setCancelled(true);
            }
    }
}
