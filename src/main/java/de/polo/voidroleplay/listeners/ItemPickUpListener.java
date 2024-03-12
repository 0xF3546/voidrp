package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class ItemPickUpListener implements Listener {
    public ItemPickUpListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().getType().equals(Material.SKELETON_SKULL) || event.getItem().getItemStack().getType().equals(Material.PLAYER_HEAD)) {
            event.setCancelled(true);
        }
    }
}
