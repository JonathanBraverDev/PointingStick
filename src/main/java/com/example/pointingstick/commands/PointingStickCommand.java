package com.example.pointingstick.commands;

import com.example.pointingstick.PointingStick;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PointingStickCommand implements CommandExecutor {

    public static final Component TOOL_NAME = Component.text("Pointing Stick").color(NamedTextColor.YELLOW);

    /** Returns true if the given ItemStack is a Pointing Stick. */
    public static boolean isPointingStick(ItemStack item) {
        return item != null
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().displayName().equals(TOOL_NAME);
    }

    /** Returns true if the player has a Pointing Stick anywhere in their inventory (including off-hand). */
    public static boolean hasStick(Player player) {
        return hasStick(player.getInventory());
    }

    private static boolean hasStick(Inventory inventory) {
        for (ItemStack item : inventory) {
            if (isPointingStick(item)) return true;
        }
        return false;
    }

    /**
     * Finds and removes any dropped Pointing Stick owned by the given player across all loaded chunks.
     * If the stick is in an unloaded chunk it won't be found — that case is handled gracefully
     * by the pickup handler, which destroys it when the owner tries to pick it up while already holding one.
     */
    private static void removeDroppedStick(Player player) {
        String uuidStr = player.getUniqueId().toString();
        for (World world : Bukkit.getWorlds()) {
            for (Item entity : world.getEntitiesByClass(Item.class)) {
                if (!isPointingStick(entity.getItemStack())) continue;
                String owner = entity.getPersistentDataContainer()
                        .get(PointingStick.OWNER_KEY, PersistentDataType.STRING);
                if (uuidStr.equals(owner)) {
                    entity.remove();
                    return;
                }
            }
        }
    }

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

        // If the player already has a stick in their inventory let them know
        if (hasStick(player)) {
            player.sendMessage(Component.text("You already have a Pointing Stick!").color(NamedTextColor.YELLOW));
            return true;
        }

        // Destroy any dropped stick they previously threw (loaded chunks only)
        removeDroppedStick(player);

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
