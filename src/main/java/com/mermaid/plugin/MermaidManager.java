package com.mermaid.plugin;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MermaidManager {

    private final JavaPlugin plugin;
    private final NamespacedKey mermaidKey;

    // Players currently in "mermaid mode"
    private final Set<UUID> activeMermaids = new HashSet<>();

    private BukkitTask task;

    // Each effect lasts 10 seconds (200 ticks) to avoid visual flicker on refresh
    private static final int DURATION_TICKS = 200;

    // Effects are refreshed every 5 seconds (100 ticks) - well before the 10s runs out
    private static final long REFRESH_INTERVAL_TICKS = 100L;

    public MermaidManager(JavaPlugin plugin, NamespacedKey mermaidKey) {
        this.plugin = plugin;
        this.mermaidKey = mermaidKey;
    }

    public void startTask() {
        // Check every half second instead of using PlayerMoveEvent, to reduce server load
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 10L);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (activeMermaids.contains(p.getUniqueId())) {
                deactivate(p);
            }
        }
    }

    private int ticksSinceLastRefresh = 0;

    private void tick() {
        ticksSinceLastRefresh += 10;
        boolean shouldRefreshEffects = ticksSinceLastRefresh >= REFRESH_INTERVAL_TICKS;
        if (shouldRefreshEffects) ticksSinceLastRefresh = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean inWater = player.isInWater() || player.getLocation().getBlock().isLiquid();
            boolean hasCharm = hasMermaidCharm(player);
            boolean isActive = activeMermaids.contains(player.getUniqueId());

            if (inWater && hasCharm) {
                if (!isActive || shouldRefreshEffects) {
                    activate(player);
                }
            } else if (isActive) {
                deactivate(player);
            }
        }
    }

    private boolean hasMermaidCharm(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (MermaidItemUtil.isMermaidCharm(item, mermaidKey)) {
                return true;
            }
        }
        return false;
    }

    private void activate(Player player) {
        boolean firstTime = !activeMermaids.contains(player.getUniqueId());
        activeMermaids.add(player.getUniqueId());

        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, DURATION_TICKS, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, DURATION_TICKS, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, DURATION_TICKS, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, DURATION_TICKS, 0, true, false));

        if (firstTime) {
            player.sendMessage("§d✦ You have transformed into a mermaid! ✦");
            // Note: no visual/shape change yet (no boots swap) - pending resource pack decision
        }
    }

    private void deactivate(Player player) {
        activeMermaids.remove(player.getUniqueId());

        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.STRENGTH);

        player.sendMessage("§7You are back to normal.");
    }

    public void handleQuit(Player player) {
        activeMermaids.remove(player.getUniqueId());
    }
}
