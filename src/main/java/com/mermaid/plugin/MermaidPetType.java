package com.mermaid.plugin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

/**
 * The 4 pets a player can choose from in the /mermaid menu.
 * "NAUTILUS" isn't a real Minecraft mob, so we represent it visually
 * using a Turtle (it already has a shell, which fits the theme).
 */
public enum MermaidPetType {

    DOLPHIN(EntityType.DOLPHIN, Material.DOLPHIN_SPAWN_EGG, "&bPet Dolphin"),
    AXOLOTL(EntityType.AXOLOTL, Material.AXOLOTL_SPAWN_EGG, "&dPet Axolotl"),
    GLOW_SQUID(EntityType.GLOW_SQUID, Material.GLOW_SQUID_SPAWN_EGG, "&ePet Glow Squid"),
    NAUTILUS(EntityType.TURTLE, Material.TURTLE_SPAWN_EGG, "&aPet Nautilus");

    private final EntityType entityType;
    private final Material menuIcon;
    private final String displayName; // uses & color codes

    MermaidPetType(EntityType entityType, Material menuIcon, String displayName) {
        this.entityType = entityType;
        this.menuIcon = menuIcon;
        this.displayName = displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Material getMenuIcon() {
        return menuIcon;
    }

    public String getDisplayName() {
        return displayName;
    }
}
