package dev.francies.betterduels.kits;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
    private final BetterDuels plugin;

    public InventoryClickListener(BetterDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("GUI.name")))) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String kitName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                String permission = "betterduels.kit." + kitName.toLowerCase();

                if (player.hasPermission(permission)) {
                    if (plugin.getKitManager().kitExists(kitName)) {
                        if (!plugin.getDuelManager().hasSelectedKit(player)) {
                            plugin.getDuelManager().storeKitSelection(player, kitName);
                            player.closeInventory();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.kit-selected").replace("%kit%", kitName)));
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.kit-not-exist")));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-perm-kit")));
                }
            }
        }
    }

}
