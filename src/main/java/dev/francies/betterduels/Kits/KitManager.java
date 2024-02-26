package dev.francies.betterduels.Kits;

import dev.francies.betterduels.BetterDuels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitManager {
    private final BetterDuels plugin;
    private final Map<String, ItemStack[]> kits;
    private final FileConfiguration config;
    private final Map<Player, ItemStack[]> playerInventoryBackup;

    public KitManager(BetterDuels plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        this.config = plugin.getConfig();
        this.playerInventoryBackup = new HashMap<>();
        loadKits();
    }

    public void loadKits() {
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) return;

        for (String kitName : kitsSection.getKeys(false)) {
            List<Map<?, ?>> kitDataList = kitsSection.getMapList(kitName);
            ItemStack[] items = new ItemStack[kitDataList.size()];
            int index = 0;

            for (Map<?, ?> kitData : kitDataList) {
                ItemStack item = parseItemStack(kitData);
                if (item != null) {
                    items[index++] = item;
                }
            }

            kits.put(kitName.toLowerCase(), items);
        }
    }

    private ItemStack parseItemStack(Map<?, ?> data) {
        Material material = Material.matchMaterial(data.get("material").toString());
        if (material == null) return null;

        int amount = 1; // Default value
        if (data.containsKey("amount")) {
            Object amountObj = data.get("amount");
            if (amountObj instanceof Integer) {
                amount = (int) amountObj;
            }
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (data.containsKey("name")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', data.get("name").toString()));
        }
        if (data.containsKey("lore")) {
            List<String> lore = (List<String>) data.get("lore");
            List<String> translatedLore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(translatedLore);
        }
        if (data.containsKey("enchantments")) {
            Map<?, ?> enchantmentsMap = (Map<?, ?>) data.get("enchantments");
            enchantmentsMap.forEach((enchantmentKey, levelObj) -> {
                if (enchantmentKey instanceof String && levelObj instanceof Integer) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(((String) enchantmentKey).toLowerCase()));
                    if (enchantment != null) {
                        int level = ((Integer) levelObj).intValue(); // Converting Object to int
                        meta.addEnchant(enchantment, level, true);
                    }
                }
            });
        }

        item.setItemMeta(meta);
        return item;
    }


    public boolean kitExists(String kitName) {
        return kits.containsKey(kitName.toLowerCase());
    }

    public void giveKit(Player player1, String kitName) {
        if (!kitExists(kitName)) return;

        // Salva l'inventario dei giocatori prima di dare il kit
        backupInventory(player1);

        ItemStack[] items = kits.get(kitName.toLowerCase());
        player1.getInventory().clear();
        player1.getInventory().setContents(items);
    }

    private void backupInventory(Player player) {
        playerInventoryBackup.put(player, player.getInventory().getContents().clone());
    }

}
