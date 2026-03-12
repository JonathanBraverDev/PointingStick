package com.example.pointingstick.listeners;

import com.example.pointingstick.PointingStick;
import com.example.pointingstick.commands.PointingStickCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

            // Support both hands — get item from whichever hand triggered the event
            org.bukkit.inventory.EquipmentSlot hand = event.getHand();
            ItemStack item = (hand == org.bukkit.inventory.EquipmentSlot.OFF_HAND)
                    ? player.getInventory().getItemInOffHand()
                    : player.getInventory().getItemInMainHand();

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

                Block clickedBlock = event.getClickedBlock();
                BlockFace clickedFace = event.getBlockFace();

                // Only allow pinging directly clicked blocks — no air pinging
                if (clickedBlock == null || clickedFace == null) {
                    player.sendMessage(Component.text("Target is too far away to ping!").color(NamedTextColor.RED));
                    return;
                }

                cooldowns.put(playerId, System.currentTimeMillis());
                createPing(clickedBlock, clickedFace, player);
            }
        }
    }

    private void createPing(Block block, BlockFace face, Player pinger) {
        Location center = block.getLocation().add(0.5, 0.5, 0.5).add(face.getDirection().multiply(0.51));
        
        // Play sound
        center.getWorld().playSound(center, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        // Initial burst
        center.getWorld().spawnParticle(Particle.END_ROD, center, 15, 0.1, 0.1, 0.1, 0.05);

        // Send a message
        for (Player p : center.getWorld().getPlayers()) {
            if (p.getLocation().distance(center) <= 50) {
                p.sendMessage(Component.text(pinger.getName() + " pinged a location!").color(NamedTextColor.YELLOW));
            }
        }

        // Periodic face highlight
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    cancel();
                    return;
                }

                spawnFaceEdges(block, face);
                ticks += 10; // Every 1/2 second
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void spawnFaceEdges(Block block, BlockFace face) {
        Location min = block.getLocation();
        double offset = 0.02; // Small offset to be outside the block
        
        // Define corners and step vectors based on face
        Location start;
        org.bukkit.util.Vector axis1, axis2;
        
        switch (face) {
            case UP:
                start = min.clone().add(0, 1 + offset, 0);
                axis1 = new org.bukkit.util.Vector(1, 0, 0);
                axis2 = new org.bukkit.util.Vector(0, 0, 1);
                break;
            case DOWN:
                start = min.clone().add(0, -offset, 0);
                axis1 = new org.bukkit.util.Vector(1, 0, 0);
                axis2 = new org.bukkit.util.Vector(0, 0, 1);
                break;
            case NORTH:
                start = min.clone().add(0, 0, -offset);
                axis1 = new org.bukkit.util.Vector(1, 0, 0);
                axis2 = new org.bukkit.util.Vector(0, 1, 0);
                break;
            case SOUTH:
                start = min.clone().add(0, 0, 1 + offset);
                axis1 = new org.bukkit.util.Vector(1, 0, 0);
                axis2 = new org.bukkit.util.Vector(0, 1, 0);
                break;
            case EAST:
                start = min.clone().add(1 + offset, 0, 0);
                axis1 = new org.bukkit.util.Vector(0, 0, 1);
                axis2 = new org.bukkit.util.Vector(0, 1, 0);
                break;
            case WEST:
                start = min.clone().add(-offset, 0, 0);
                axis1 = new org.bukkit.util.Vector(0, 0, 1);
                axis2 = new org.bukkit.util.Vector(0, 1, 0);
                break;
            default:
                return;
        }

        drawEdge(start, axis1);
        drawEdge(start, axis2);
        drawEdge(start.clone().add(axis1), axis2);
        drawEdge(start.clone().add(axis2), axis1);
    }

    private void drawEdge(Location start, org.bukkit.util.Vector direction) {
        for (double d = 0; d <= 1.0; d += 0.2) {
            Location loc = start.clone().add(direction.clone().multiply(d));
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);
        }
    }
}
