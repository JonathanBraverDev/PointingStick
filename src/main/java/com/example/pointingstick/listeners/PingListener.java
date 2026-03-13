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
import org.bukkit.persistence.PersistentDataType;
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

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK ||
            action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

            org.bukkit.inventory.EquipmentSlot hand = event.getHand();
            ItemStack item = (hand == org.bukkit.inventory.EquipmentSlot.OFF_HAND)
                    ? player.getInventory().getItemInOffHand()
                    : player.getInventory().getItemInMainHand();

            if (PointingStickCommand.isPointingStick(item)) {
                event.setCancelled(true);

                UUID playerId = player.getUniqueId();
                if (cooldowns.containsKey(playerId)) {
                    long timeLeft = (cooldowns.get(playerId) + COOLDOWN_TIME_MS) - System.currentTimeMillis();
                    if (timeLeft > 0) return;
                }

                Block targetBlock = player.getTargetBlockExact(50);
                BlockFace targetFace = player.getTargetBlockFace(50);

                if (targetBlock == null || targetFace == null) {
                    player.sendMessage(Component.text("Target is too far away to ping!").color(NamedTextColor.RED));
                    return;
                }

                cooldowns.put(playerId, System.currentTimeMillis());
                createPing(targetBlock, targetFace, player);
            }
        }
    }

    private void createPing(Block block, BlockFace face, Player pinger) {
        Location center = block.getLocation().add(0.5, 0.5, 0.5).add(face.getDirection().multiply(0.51));
        
        // Get settings
        String colorName = pinger.getPersistentDataContainer().get(PointingStick.COLOR_KEY, PersistentDataType.STRING);
        NamedTextColor color = (colorName != null) ? NamedTextColor.NAMES.value(colorName.toLowerCase()) : NamedTextColor.YELLOW;
        if (color == null) color = NamedTextColor.YELLOW;

        String soundName = pinger.getPersistentDataContainer().get(PointingStick.SOUND_KEY, PersistentDataType.STRING);
        Sound pingSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        if (soundName != null) {
            try {
                pingSound = Sound.valueOf(soundName);
            } catch (IllegalArgumentException ignored) {}
        }

        org.bukkit.Color bukkitColor = org.bukkit.Color.fromRGB(color.value());
        Particle.DustOptions dust = new Particle.DustOptions(bukkitColor, 1.2f);

        // Play sound
        center.getWorld().playSound(center, pingSound, 1.0f, 1.0f);

        // Initial burst
        center.getWorld().spawnParticle(Particle.DUST, center, 15, 0.1, 0.1, 0.1, 0.05, dust);

        // Send a message
        Component msg = Component.text(pinger.getName() + " pinged a location!").color(color);
        for (Player p : center.getWorld().getPlayers()) {
            if (p.getLocation().distance(center) <= 50) {
                p.sendMessage(msg);
            }
        }

        // Periodic face highlight
        NamedTextColor finalColor = color;
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    cancel();
                    return;
                }

                spawnFaceEdges(block, face, finalColor);
                ticks += 10; // Every 1/2 second
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void spawnFaceEdges(Block block, BlockFace face, NamedTextColor color) {
        Location min = block.getLocation();
        double offset = 0.02;
        
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

        drawEdge(start, axis1, color);
        drawEdge(start, axis2, color);
        drawEdge(start.clone().add(axis1), axis2, color);
        drawEdge(start.clone().add(axis2), axis1, color);
    }

    private void drawEdge(Location start, org.bukkit.util.Vector direction, NamedTextColor color) {
        org.bukkit.Color bukkitColor = org.bukkit.Color.fromRGB(color.value());
        Particle.DustOptions dust = new Particle.DustOptions(bukkitColor, 0.8f);

        for (double d = 0; d <= 1.0; d += 0.2) {
            Location loc = start.clone().add(direction.clone().multiply(d));
            loc.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dust);
        }
    }
}
