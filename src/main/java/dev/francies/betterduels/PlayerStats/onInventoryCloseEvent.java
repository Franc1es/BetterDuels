package dev.francies.betterduels.PlayerStats;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class onInventoryCloseEvent implements Listener {
    private final BetterDuels plugin;

    public onInventoryCloseEvent(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("GUI.name")))) {
            Player player = (Player) event.getPlayer();
            if (!plugin.getDuelManager().hasSelectedKit(player)) {
                plugin.getDuelManager().cancelDuel(player);
            }
        }
    }
}
