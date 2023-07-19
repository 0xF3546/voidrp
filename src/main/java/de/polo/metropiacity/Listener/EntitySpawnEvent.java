package de.polo.metropiacity.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntitySpawnEvent implements Listener {
    @EventHandler
    public void onEntitySpawn(org.bukkit.event.entity.EntitySpawnEvent event) {
        event.setCancelled(true);
    }
}
