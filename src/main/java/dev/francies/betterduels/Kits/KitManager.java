package dev.francies.betterduels.Kits;

import dev.francies.betterduels.BetterDuels;
import dev.francies.betterduels.Mess.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitManager {
    private final BetterDuels plugin;
    private final Map<String, ItemStack[]> kits;
    private final Map<Player, ItemStack[]> playerInventoryBackup;

    public KitManager(BetterDuels plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        this.playerInventoryBackup = new HashMap<>();
        loadKits();
    }

    private void loadKits() {
        ConfigurationSection kitsSection = plugin.getConfig().getConfigurationSection("kits");
        if (kitsSection == null) return;

        for (String kitName : kitsSection.getKeys(false)) {
            List<Map<?, ?>> kitItemsList = kitsSection.getMapList(kitName);
            ItemStack[] kitItems = new ItemStack[kitItemsList.size()];
            int index = 0;
            for (Map<?, ?> itemData : kitItemsList) {
                ItemStack item = createItemStackFromMap(itemData);
                if (item != null) {
                    kitItems[index++] = item;
                }
            }
            kits.put(kitName.toLowerCase(), kitItems);
        }
    }

    private ItemStack createItemStackFromMap(Map<?, ?> map) {
        Material material = Material.matchMaterial((String) map.get("material"));
        if (material == null) {
            return null;
        }
        int amount = map.containsKey("amount") ? (Integer) map.get("amount") : 1;
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (map.containsKey("name")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) map.get("name")));
        }
        if (map.containsKey("lore")) {
            List<String> lore = (List<String>) map.get("lore");
            meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
        }
        if (map.containsKey("enchantments")) {
            Map<String, Integer> enchantments = (Map<String, Integer>) map.get("enchantments");
            for (Map.Entry<String, Integer> enchantmentEntry : enchantments.entrySet()) {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentEntry.getKey().toLowerCase()));
                if (enchantment != null) {
                    meta.addEnchant(enchantment, enchantmentEntry.getValue(), true);
                }
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    public boolean kitExists(String kitName) {
        return kits.containsKey(kitName.toLowerCase());
    }

    public void giveKit(Player player, String kitName) {
        if (!kitExists(kitName)) return;
        backupInventory(player);
        ItemStack[] kitItems = kits.get(kitName.toLowerCase());
        player.getInventory().clear();
        player.getInventory().setContents(kitItems);
    }

    private void backupInventory(Player player) {
        playerInventoryBackup.put(player, player.getInventory().getContents().clone());
    }
    public void openKitSelectionGUI(Player player) {

        Inventory inv = Bukkit.createInventory(null, plugin.getConfig().getInt("GUI.size") ,ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("GUI.name")));


        for (String kitName : kits.keySet()) {

            ItemStack kitItem = new ItemStack(Material.PAPER);
            ItemMeta meta = kitItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + kitName);

            kitItem.setItemMeta(meta);
            inv.addItem(kitItem);
        }



        player.openInventory(inv);
    }
}
