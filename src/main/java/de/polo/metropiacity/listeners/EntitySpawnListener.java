package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntitySpawnListener implements Listener {
    public EntitySpawnListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onEntitySpawn(org.bukkit.event.entity.EntitySpawnEvent event) {
        event.setCancelled(true);
    }
}
