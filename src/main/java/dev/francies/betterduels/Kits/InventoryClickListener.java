package dev.francies.betterduels.Kits;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Mess.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.UUID;

public class InventoryClickListener implements Listener {
    private final BetterDuels plugin;
    private HashSet<UUID> playersWhoSelectedKit = new HashSet<>();
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
                UUID playerUUID = player.getUniqueId();
                if (playersWhoSelectedKit.contains(playerUUID)) {
                    player.sendMessage(ChatColor.RED + "Hai già selezionato un kit!");
                    return;
                }

                String kitName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

                String permission = "betterduels.kit." + kitName.toLowerCase();
                if (player.hasPermission(permission)) {
                    if (plugin.getKitManager().kitExists(kitName)) {
                        plugin.getKitManager().giveKit(player, kitName);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("kit-received").replace("%kit%", kitName)));
                        playersWhoSelectedKit.add(playerUUID); // Aggiungi l'UUID del giocatore all'HashSet dopo la selezione del kit
                    } else {
                        player.sendMessage(ChatColor.RED + "Questo kit non esiste.");
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("no-perm-kit")));
                }
            }
        }
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("GUI.name")))) {
            Player player = (Player) event.getPlayer();

            if (plugin.getDuelManager().isInDuel(player)) {

                plugin.getDuelManager().markKitSelection(player, true);


                Player otherPlayer = plugin.getDuelManager().getOtherPlayer(player);

                if (plugin.getDuelManager().hasSelectedKit(player) && plugin.getDuelManager().hasSelectedKit(otherPlayer)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("kit-selected")));
                    otherPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("kit-selected")));
                    plugin.getDuelManager().kitSelected(player);
                    plugin.getDuelManager().kitSelected(otherPlayer);
                    plugin.getDuelManager().setGameMode(player, otherPlayer);
                } else {
                    plugin.getDuelManager().setGameMode1(player, otherPlayer);
                    if (!plugin.getDuelManager().hasSelectedKit(player)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("choose-kit")));
                    }
                    if (!plugin.getDuelManager().hasSelectedKit(otherPlayer)) {
                        otherPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.get("prefix") + Messages.get("wait-opponent")));
                    }
                }
            }
        }
    }


}
