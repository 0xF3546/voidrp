package de.polo.metropiacity.Listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class itemPickUpListener implements Listener {

        @EventHandler
        public void onItemPickup(EntityPickupItemEvent event) {
            if (event.getItem().getItemStack().getType().equals(Material.SKELETON_SKULL) || event.getItem().getItemStack().getType().equals(Material.PLAYER_HEAD)) {
                event.setCancelled(true);
            }
        }
}
