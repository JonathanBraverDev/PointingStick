package com.example.pointingstick.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    public static final Component TOOL_NAME = Component.text("Pointing Stick").color(NamedTextColor.YELLOW);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("pointingstick.use")) {
            player.sendMessage(Component.text("You do not have permission to use the Pointing Stick.").color(NamedTextColor.RED));
            return true;
        }

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.displayName(TOOL_NAME);
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Right-click or Left-click while").color(NamedTextColor.GRAY));
            lore.add(Component.text("looking at a block to ping it!").color(NamedTextColor.GRAY));
            meta.lore(lore);
            stick.setItemMeta(meta);
        }

        player.getInventory().addItem(stick);
        player.sendMessage(Component.text("You have received the Pointing Stick!").color(NamedTextColor.GREEN));

        return true;
    }
}
