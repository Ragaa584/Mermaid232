package com.mermaid.plugin;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Makes pets behave like a tamed wolf:
 *  - if the owner is attacked, the pet goes after the attacker
 *  - if the owner attacks something, the pet joins in
 *
 * Vanilla dolphins/axolotls/squids/turtles don't ship with a generic
 * melee-attack AI goal, so instead of relying on that we manually walk
 * the pet toward the target using Paper's Mob#getPathfinder() and deal
 * damage once it's close enough. This keeps everything on the Bukkit/
 * Paper API - no NMS needed.
 */
public class MermaidPetCombatListener implements Listener {

    private final JavaPlugin plugin;
    private final MermaidPetManager petManager;

    private static final double ATTACK_RANGE = 1.6;
    private static final double PET_DAMAGE = 2.0; // 1 heart per hit
    private static final int ENGAGE_TIMEOUT_TICKS = 20 * 10; // give up after 10s

    public MermaidPetCombatListener(JavaPlugin plugin, MermaidPetManager petManager) {
        this.plugin = plugin;
        this.petManager = petManager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        // Case 1: owner got hit -> pet defends
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            engagePetFor(victim.getUniqueId(), attacker);
        }

        // Case 2: owner attacked something -> pet joins in
        if (event.getDamager() instanceof Player owner && event.getEntity() instanceof LivingEntity target) {
            engagePetFor(owner.getUniqueId(), target);
        }
    }

    private void engagePetFor(UUID ownerUuid, LivingEntity target) {
        LivingEntity pet = petManager.getActivePet(ownerUuid);
        if (pet == null || pet.isDead() || !(pet instanceof Mob mob)) return;
        if (target == pet) return; // don't attack its own owner's pet by accident
        if (!petManager.isOwnedPet(pet)) return;

        runEngagement(mob, target);
    }

    private void runEngagement(Mob pet, LivingEntity target) {
        new BukkitRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                ticksElapsed += 5;

                if (pet.isDead() || target.isDead() || !target.isValid()
                        || !pet.getWorld().equals(target.getWorld())
                        || ticksElapsed > ENGAGE_TIMEOUT_TICKS) {
                    cancel();
                    return;
                }

                double distance = pet.getLocation().distance(target.getLocation());

                if (distance <= ATTACK_RANGE) {
                    target.damage(PET_DAMAGE, pet);
                    cancel(); // one hit per engagement is enough to feel responsive; next damage event re-triggers it
                    return;
                }

                pet.getPathfinder().moveTo(target, 1.2);
            }
        }.runTaskTimer(plugin, 0L, 5L); // check 4x per second
    }
}
