package com.mermaid.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class MermaidListener implements Listener {

    private final NamespacedKey mermaidKey;

    public MermaidListener(NamespacedKey mermaidKey) {
        this.mermaidKey = mermaidKey;
    }

    // Prevent dropping the charm on the ground
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (MermaidItemUtil.isMermaidCharm(dropped, mermaidKey)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou can't drop the Mermaid Charm!");
        }
    }

    // Prevent moving the charm into any other inventory (chest, shulker, villager trade, etc.)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean movingMermaidItem =
                (current != null && MermaidItemUtil.isMermaidCharm(current, mermaidKey)) ||
                (cursor != null && MermaidItemUtil.isMermaidCharm(cursor, mermaidKey));

        if (!movingMermaidItem) return;

        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        // If the open inventory isn't the player's own inventory (chest/shulker/trade/furnace...)
        boolean topIsPlayerInventory = topInventory instanceof PlayerInventory;

        if (!topIsPlayerInventory) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage("§cYou can't move the Mermaid Charm into another inventory.");
            }
        }

        // Also block shift-click, which could auto-move it into an open chest
        if (clickedInventory instanceof PlayerInventory && !topIsPlayerInventory && event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    // Clean up any temporary data when a player leaves (avoid memory leaks)
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Note: MermaidManager keeps its own Set/Map; if you want extra cleanup here later,
        // this is a sensible place to add it. Currently the tick loop handles it fine on its
        // own since it only iterates over getOnlinePlayers().
    }
}
