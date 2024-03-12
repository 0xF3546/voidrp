package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {
    public EntitySpawnListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity().getType() == EntityType.VILLAGER ||event.getEntity().getType() == EntityType.MINECART) return;
        if (event.getEntity() instanceof Animals || event.getEntity() instanceof Mob) {
            event.setCancelled(true);
        }
    }
}
