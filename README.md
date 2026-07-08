# MermaidPlugin

A Minecraft plugin (Paper 1.21.11) that turns a player into a "mermaid" while they're in water and holding a special item.

## What the plugin does

- `/mermaid give <player>` — gives the target a "Mermaid Charm" (a special purple-named Nautilus Shell). Only usable by someone with the `mermaid.give` permission (op by default).
- `/mermaid remove <player>` — removes the charm from the target.
- While a player is **in water** and **holding the Charm** anywhere in their inventory:
  - They get: Dolphin's Grace (swim speed), Water Breathing, Night Vision, and Strength — each effect lasts 10 seconds and auto-refreshes every 5 seconds, so it never runs low and flickers.
  - **No shape/visual change yet** (no boots swap or anything) — the visual "tail" was removed until we decide on a resource pack, since any real visual "layer" on top of the player requires one.
  - As soon as they leave the water or lose the charm, all effects are removed instantly.
- The charm can't be crafted, dropped, or traded/moved into another inventory (chest, shulker, villager, etc). The only way to get it is via the `/mermaid give` command.

## ⚠️ Important note about the tail's visual shape

There is no Bukkit/Paper API that lets you change a player's actual body model into a fish tail. To get a real 3D tail visual you'd need either:
1. A **custom Resource Pack** (made in Blockbench) tied to a custom model on a boot/item.
2. An external library like **ModelEngine**.

Let me know if you'd like to move forward with either approach.

## Build instructions (on your machine)

1. Install **JDK 17 or newer** and **Maven**.
2. Open a terminal inside the project folder (where `pom.xml` is).
3. Run:
   ```bash
   mvn clean package
   ```
4. The resulting jar will be at: `target/MermaidPlugin.jar`

## Testing on your local server

1. Make sure your server is Paper 1.21.11 (not Spigot or Vanilla).
2. Put `MermaidPlugin.jar` inside the server's `plugins/` folder.
3. Start (or restart) the server.
4. Confirm you see the log line: `MermaidPlugin enabled! ✔`
5. Join the game with an op account and type:
   ```
   /mermaid give <your name>
   ```
6. Jump into any lake/ocean while holding the charm anywhere in your inventory (doesn't need to be in hand).
7. You should immediately feel the speed boost underwater and see the potion effect icons.
8. Try dropping the charm or putting it in a chest → it should be blocked.
9. Leave the water or remove the charm → all effects should be removed right away.

## Project structure

```
mermaid-plugin/
├── pom.xml
└── src/main/
    ├── resources/plugin.yml
    └── java/com/mermaid/plugin/
        ├── MermaidPlugin.java      (main class)
        ├── MermaidItemUtil.java    (creates & checks the charm item)
        ├── MermaidCommand.java     (/mermaid command)
        ├── MermaidManager.java     (condition check + effects)
        └── MermaidListener.java    (prevents dropping/moving the charm)
```
