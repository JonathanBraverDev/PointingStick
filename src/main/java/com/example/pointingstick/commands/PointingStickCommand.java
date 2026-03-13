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

import org.bukkit.command.TabCompleter;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Arrays;

public class PointingStickCommand implements CommandExecutor, TabCompleter {

    public static final Component TOOL_NAME = Component.text("Pointing Stick").color(NamedTextColor.YELLOW);

    /** Returns true if the given ItemStack is a Pointing Stick. */
    public static boolean isPointingStick(ItemStack item) {
        return item != null
                && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(PointingStick.IS_STICK_KEY, PersistentDataType.BYTE);
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("pointingstick.use")) {
            player.sendMessage(Component.text("You do not have permission to use the Pointing Stick.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("color")) {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /pointingstick color <color_name>").color(NamedTextColor.RED));
                    return true;
                }
                String colorName = args[1].toUpperCase();
                NamedTextColor color = NamedTextColor.NAMES.value(colorName.toLowerCase());
                if (color == null) {
                    player.sendMessage(Component.text("Invalid color name!").color(NamedTextColor.RED));
                    return true;
                }
                player.getPersistentDataContainer().set(PointingStick.COLOR_KEY, PersistentDataType.STRING, colorName);
                player.sendMessage(Component.text("Your ping color has been set to ").color(NamedTextColor.GREEN)
                        .append(Component.text(colorName).color(color)));
                
                updateStickInInventory(player);
                return true;
            } else if (args[0].equalsIgnoreCase("sound")) {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /pointingstick sound <sound_name>").color(NamedTextColor.RED));
                    return true;
                }
                String soundName = args[1].toUpperCase();
                try {
                    org.bukkit.Sound.valueOf(soundName);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Component.text("Invalid sound name!").color(NamedTextColor.RED));
                    return true;
                }
                player.getPersistentDataContainer().set(PointingStick.SOUND_KEY, PersistentDataType.STRING, soundName);
                player.sendMessage(Component.text("Your ping sound has been set to ").color(NamedTextColor.GREEN)
                        .append(Component.text(soundName).color(NamedTextColor.YELLOW)));
                return true;
            }
        }

        // Default: Give the stick
        if (hasStick(player)) {
            player.sendMessage(Component.text("You already have a Pointing Stick!").color(NamedTextColor.YELLOW));
            return true;
        }

        removeDroppedStick(player);
        player.getInventory().addItem(createStick(player));
        player.sendMessage(Component.text("You have received the Pointing Stick!").color(NamedTextColor.GREEN));

        return true;
    }

    private ItemStack createStick(Player player) {
        String colorName = player.getPersistentDataContainer().get(PointingStick.COLOR_KEY, PersistentDataType.STRING);
        NamedTextColor color = (colorName != null) ? NamedTextColor.NAMES.value(colorName.toLowerCase()) : NamedTextColor.YELLOW;
        if (color == null) color = NamedTextColor.YELLOW;

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Pointing Stick").color(color));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Right-click or Left-click while").color(NamedTextColor.GRAY));
            lore.add(Component.text("looking at a block to ping it!").color(NamedTextColor.GRAY));
            meta.lore(lore);
            
            // Mark it as a pointing stick
            meta.getPersistentDataContainer().set(PointingStick.IS_STICK_KEY, PersistentDataType.BYTE, (byte) 1);
            
            stick.setItemMeta(meta);
        }
        return stick;
    }

    private void updateStickInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isPointingStick(item)) {
                String colorName = player.getPersistentDataContainer().get(PointingStick.COLOR_KEY, PersistentDataType.STRING);
                NamedTextColor color = (colorName != null) ? NamedTextColor.NAMES.value(colorName.toLowerCase()) : NamedTextColor.YELLOW;
                if (color == null) color = NamedTextColor.YELLOW;

                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text("Pointing Stick").color(color));
                item.setItemMeta(meta);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("color", "sound").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("color")) {
                return NamedTextColor.NAMES.keys().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("sound")) {
                return Arrays.stream(org.bukkit.Sound.values())
                        .map(s -> s.name())
                        .filter(s -> s.startsWith(args[1].toUpperCase()))
                        .limit(20)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
