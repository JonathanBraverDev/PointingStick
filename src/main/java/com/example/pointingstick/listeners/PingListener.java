package com.example.pointingstick.listeners;

import com.example.pointingstick.PointingStick;
import com.example.pointingstick.commands.PointingStickCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PingListener implements Listener {

    private final PointingStick plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_TIME_MS = 2000; // 2 seconds

    public PingListener(PointingStick plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        // Check if it's a left or right click
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK ||
            action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if holding the Pointing Stick
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().displayName().equals(PointingStickCommand.TOOL_NAME)) {

                // Cancel the event so they don't break blocks or interact with things accidentally
                event.setCancelled(true);

                // Handle Cooldown
                UUID playerId = player.getUniqueId();
                if (cooldowns.containsKey(playerId)) {
                    long timeLeft = (cooldowns.get(playerId) + COOLDOWN_TIME_MS) - System.currentTimeMillis();
                    if (timeLeft > 0) {
                        return; // Still on cooldown
                    }
                }

                // Get the block they are looking at (max distance 100 blocks)
                Block targetBlock = player.getTargetBlockExact(100);

                if (targetBlock == null) {
                    player.sendMessage(Component.text("Target is too far away to ping!").color(NamedTextColor.RED));
                    return;
                }

                cooldowns.put(playerId, System.currentTimeMillis());

                createPing(targetBlock.getLocation().add(0.5, 1.0, 0.5), player);
            }
        }
    }

    private void createPing(Location location, Player pinger) {
        // Spawn an invisible ArmorStand to hold the glow effect
        ArmorStand indicator = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        indicator.setVisible(false);
        indicator.setGravity(false);
        indicator.setMarker(true);
        indicator.setGlowing(true);

        // Play sound to players nearby
        location.getWorld().playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        // Send a message
        for (Player p : location.getWorld().getPlayers()) {
            if (p.getLocation().distance(location) <= 50) {
                p.sendMessage(Component.text(pinger.getName() + " pinged a location!").color(NamedTextColor.YELLOW));
            }
        }

        // Create a task to spawn particles and remove the indicator after 5 seconds
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds (20 ticks per second)
                    indicator.remove();
                    cancel();
                    return;
                }

                // Spawn some particles around the pinged location
                location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0);

                ticks += 5; // Run every 5 ticks (1/4 second)
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
