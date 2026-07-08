package com.mermaid.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class MermaidPlugin extends JavaPlugin {

    private NamespacedKey mermaidKey;
    private MermaidManager mermaidManager;

    @Override
    public void onEnable() {
        // Unique key used to tag the charm item (NBT), so we can tell it apart from a plain item
        this.mermaidKey = new NamespacedKey(this, "mermaid_charm");

        this.mermaidManager = new MermaidManager(this, mermaidKey);

        // Register commands
        MermaidCommand commandExecutor = new MermaidCommand(this, mermaidKey);
        getCommand("mermaid").setExecutor(commandExecutor);
        getCommand("mermaid").setTabCompleter(commandExecutor);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MermaidListener(mermaidKey), this);

        // Start the periodic check (every half second) instead of relying on PlayerMoveEvent
        mermaidManager.startTask();

        // --- Mermaid Pets add-on (new) ---
        MermaidPetManager.init(this);
        getServer().getPluginManager().registerEvents(
                new MermaidPetCombatListener(this, MermaidPetManager.get()), this);

        getLogger().info("MermaidPlugin enabled! ✔");
    }

    @Override
    public void onDisable() {
        if (mermaidManager != null) {
            mermaidManager.shutdown();
        }
        getLogger().info("MermaidPlugin disabled.");
    }

    public NamespacedKey getMermaidKey() {
        return mermaidKey;
    }
}
