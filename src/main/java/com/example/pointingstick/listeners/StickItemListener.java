package com.example.pointingstick.listeners;

import com.example.pointingstick.PointingStick;
import com.example.pointingstick.commands.PointingStickCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataType;

public class StickItemListener implements Listener {

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item entity = event.getItemDrop();
        if (!PointingStickCommand.isPointingStick(entity.getItemStack())) return;

        // Tag the dropped entity with the owner's UUID so only they can pick it back up
        entity.getPersistentDataContainer().set(
                PointingStick.OWNER_KEY,
                PersistentDataType.STRING,
                event.getPlayer().getUniqueId().toString()
        );
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent event) {
        Item entity = event.getItem();
        if (!PointingStickCommand.isPointingStick(entity.getItemStack())) return;

        if (!(event.getEntity() instanceof Player player)) {
            // Non-player entities cannot pick up the stick
            event.setCancelled(true);
            return;
        }

        String ownerUUID = entity.getPersistentDataContainer()
                .get(PointingStick.OWNER_KEY, PersistentDataType.STRING);
        boolean isOwner = player.getUniqueId().toString().equals(ownerUUID);

        if (isOwner && !PointingStickCommand.hasStick(player)) {
            // Owner picking up their own stick and they don't already have one — allow
            return;
        }

        // In all other cases: cancel and destroy to prevent duplication
        event.setCancelled(true);
        entity.remove();

        if (isOwner) {
            // Owner already has a stick — gently remind them
            player.sendMessage(Component.text("One Pointing Stick is more than enough power for any mortal...")
                    .color(NamedTextColor.GOLD));
        }
    }
}
