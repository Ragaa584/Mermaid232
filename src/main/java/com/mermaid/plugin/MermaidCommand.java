package com.mermaid.plugin;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MermaidCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final NamespacedKey mermaidKey;

    public MermaidCommand(JavaPlugin plugin, NamespacedKey mermaidKey) {
        this.plugin = plugin;
        this.mermaidKey = mermaidKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /mermaid with no arguments -> open the pet-choice menu for any player (no permission needed)
        if (args.length == 0) {
            if (sender instanceof Player player) {
                MermaidPetManager.get().openPetMenu(player);
            } else {
                sender.sendMessage("§cOnly players can use this.");
            }
            return true;
        }

        // give/remove are admin-only actions
        if (!sender.hasPermission("mermaid.give")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eUsage: /mermaid <give|remove> <player>");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage("§cThat player is not online.");
            return true;
        }

        switch (action) {
            case "give" -> {
                ItemStack charm = MermaidItemUtil.createMermaidCharm(mermaidKey);
                target.getInventory().addItem(charm);
                sender.sendMessage("§aGave " + target.getName() + " the Mermaid Charm.");
                target.sendMessage("§dYou received the §5Mermaid Charm§d! Try jumping into water while holding it.");
            }
            case "remove" -> {
                // Remove every copy of the charm from the target's inventory
                target.getInventory().forEach(item -> {
                    if (MermaidItemUtil.isMermaidCharm(item, mermaidKey)) {
                        item.setAmount(0);
                    }
                });
                sender.sendMessage("§aRemoved the Mermaid Charm from " + target.getName() + ".");
            }
            default -> sender.sendMessage("§eUsage: /mermaid <give|remove> <player>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("give");
            options.add("remove");
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                options.add(p.getName());
            }
        }
        return options;
    }
}
