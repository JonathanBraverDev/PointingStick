package com.example.pointingstick.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PointingStickCommand implements CommandExecutor {

    public static final String TOOL_NAME = ChatColor.YELLOW + "Pointing Stick";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("pointingstick.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use the Pointing Stick.");
            return true;
        }

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TOOL_NAME);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click or Left-click while");
            lore.add(ChatColor.GRAY + "looking at a block to ping it!");
            meta.setLore(lore);
            stick.setItemMeta(meta);
        }

        player.getInventory().addItem(stick);
        player.sendMessage(ChatColor.GREEN + "You have received the Pointing Stick!");

        return true;
    }
}
