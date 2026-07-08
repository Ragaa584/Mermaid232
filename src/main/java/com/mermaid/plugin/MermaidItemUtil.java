package com.mermaid.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MermaidItemUtil {

    /**
     * Creates the special "Mermaid Charm" item (purple name + hidden NBT tag).
     */
    public static ItemStack createMermaidCharm(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();

        // Purple display name (§5 = purple in Minecraft color codes)
        meta.setDisplayName("§5§lMermaid Charm");

        meta.setLore(java.util.List.of(
                "§7An ancient legendary charm",
                "§7Transforms you into a mermaid while in water",
                "",
                "§d✦ Speed, Strength, Water Breathing & Night Vision ✦"
        ));

        // A subtle "glow" effect (hidden fake enchant) so it stands out in hand
        meta.addEnchant(Enchantment.LURE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        // The secret tag that marks this as the real charm (not just any Nautilus Shell)
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Checks whether this ItemStack is the original Mermaid Charm (not a plain Nautilus Shell).
     */
    public static boolean isMermaidCharm(ItemStack item, NamespacedKey key) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Boolean value = meta.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        return value != null && value;
    }
}
