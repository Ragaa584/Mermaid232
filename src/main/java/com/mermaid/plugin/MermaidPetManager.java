package com.mermaid.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles everything about the Mermaid Pets add-on:
 *  - the /mermaid pet-choice menu
 *  - remembering what each player picked
 *  - spawning/despawning the pet automatically based on the same
 *    condition as the charm effects (charm in inventory + in water)
 *  - tagging spawned pets so they never get confused with wild mobs
 *
 * This class is fully independent of the existing Mermaid classes.
 * It only needs to be registered once in onEnable() (see integration
 * notes at the bottom of this file).
 */
public class MermaidPetManager implements Listener {

    private static MermaidPetManager instance;

    private final JavaPlugin plugin;
    private final NamespacedKey petOwnerKey;
    private final NamespacedKey menuItemKey;
    private final NamespacedKey charmKey; // must match the key used by MermaidItemUtil

    private final Map<UUID, MermaidPetType> playerChoice = new HashMap<>();
    private final Map<UUID, UUID> activePetByOwner = new HashMap<>(); // owner UUID -> pet entity UUID

    private static final String MENU_TITLE = ChatColor.DARK_AQUA + "Choose your Mermaid Pet";

    public MermaidPetManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petOwnerKey = new NamespacedKey(plugin, "mermaid_pet_owner");
        this.menuItemKey = new NamespacedKey(plugin, "mermaid_pet_menu_item");
        this.charmKey = new NamespacedKey(plugin, "mermaid_charm");
    }

    /** Call this once from onEnable(). */
    public static void init(JavaPlugin plugin) {
        instance = new MermaidPetManager(plugin);
        Bukkit.getPluginManager().registerEvents(instance, plugin);
        instance.startTickTask();
    }

    public static MermaidPetManager get() {
        return instance;
    }

    // ---------------------------------------------------------------
    // Menu
    // ---------------------------------------------------------------

    public void openPetMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, MENU_TITLE);
        for (MermaidPetType type : MermaidPetType.values()) {
            menu.addItem(buildMenuIcon(type));
        }
        player.openInventory(menu);
    }

    private ItemStack buildMenuIcon(MermaidPetType type) {
        ItemStack item = new ItemStack(type.getMenuIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', type.getDisplayName()));
        meta.getPersistentDataContainer().set(menuItemKey, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!MENU_TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true); // never let players take the menu items

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getItemMeta() == null) return;

        String typeName = clicked.getItemMeta().getPersistentDataContainer()
                .get(menuItemKey, PersistentDataType.STRING);
        if (typeName == null) return;

        MermaidPetType type = MermaidPetType.valueOf(typeName);
        Player player = (Player) event.getWhoClicked();
        playerChoice.put(player.getUniqueId(), type);
        player.closeInventory();
        player.sendMessage(ChatColor.AQUA + "Your mermaid pet is now: " + ChatColor.translateAlternateColorCodes('&', type.getDisplayName()));
        player.sendMessage(ChatColor.GRAY + "Get in the water with your charm and it'll appear next to you!");
    }

    // ---------------------------------------------------------------
    // Auto spawn / despawn tick task
    // ---------------------------------------------------------------

    private void startTickTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tickPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // every 1 second
    }

    private void tickPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        MermaidPetType chosen = playerChoice.get(uuid);
        boolean shouldHavePet = chosen != null && player.isInWater() && hasCharm(player);

        LivingEntity existingPet = getActivePet(uuid);

        if (!shouldHavePet) {
            if (existingPet != null) {
                existingPet.remove();
                activePetByOwner.remove(uuid);
            }
            return;
        }

        if (existingPet == null || existingPet.isDead()) {
            spawnPet(player, chosen);
        } else if (existingPet.getLocation().distance(player.getLocation()) > 20) {
            // fell too far behind (e.g. teleport) - snap back next to owner
            existingPet.teleport(player.getLocation());
        }
    }

    private boolean hasCharm(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (MermaidItemUtil.isMermaidCharm(item, charmKey)) {
                return true;
            }
        }
        return false;
    }

    private void spawnPet(Player player, MermaidPetType type) {
        Location loc = player.getLocation();
        LivingEntity pet = (LivingEntity) player.getWorld().spawnEntity(loc, type.getEntityType());

        pet.setCustomName(ChatColor.translateAlternateColorCodes('&',
                type.getDisplayName() + " &7(" + player.getName() + ")"));
        pet.setCustomNameVisible(true);
        pet.setGlowing(true);
        pet.setRemoveWhenFarAway(false);
        pet.getPersistentDataContainer().set(petOwnerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        activePetByOwner.put(player.getUniqueId(), pet.getUniqueId());
    }

    // ---------------------------------------------------------------
    // Helpers used by the combat listener too
    // ---------------------------------------------------------------

    LivingEntity getActivePet(UUID ownerUuid) {
        UUID petUuid = activePetByOwner.get(ownerUuid);
        if (petUuid == null) return null;
        org.bukkit.entity.Entity e = Bukkit.getEntity(petUuid);
        return (e instanceof LivingEntity) ? (LivingEntity) e : null;
    }

    boolean isOwnedPet(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(petOwnerKey, PersistentDataType.STRING);
    }

    UUID getOwnerOf(LivingEntity pet) {
        String raw = pet.getPersistentDataContainer().get(petOwnerKey, PersistentDataType.STRING);
        return raw == null ? null : UUID.fromString(raw);
    }

    // ---------------------------------------------------------------
    // Cleanup
    // ---------------------------------------------------------------

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        LivingEntity pet = getActivePet(uuid);
        if (pet != null) pet.remove();
        activePetByOwner.remove(uuid);
    }
}
