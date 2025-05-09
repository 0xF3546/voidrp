package de.polo.core.listeners;

import de.polo.core.manager.ItemManager;
import de.polo.core.utils.Event;
import de.polo.core.utils.enums.RoleplayItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

@Event
public class ItemPickUpListener implements Listener {

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().getType().equals(Material.SKELETON_SKULL) || event.getItem().getItemStack().getType().equals(Material.WITHER_SKELETON_SKULL) || event.getItem().getItemStack().getType().equals(Material.PLAYER_HEAD)) {
            event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player player) {
            if (ItemManager.getCustomItemCount(player, RoleplayItem.SWAT_SHIELD) >= 1 && event.getItem().getItemStack().getType().equals(RoleplayItem.SWAT_SHIELD.getMaterial())) {
                event.setCancelled(true);
            }
        }
    }
}
