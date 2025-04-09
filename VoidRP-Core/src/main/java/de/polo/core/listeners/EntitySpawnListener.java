package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.utils.Event;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

@Event
public class EntitySpawnListener implements Listener {
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity().getType() == EntityType.VILLAGER || event.getEntity().getType() == EntityType.MINECART) {
        }
        /*if (event.getEntity() instanceof Animals || event.getEntity() instanceof Mob) {
            if (event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "owner"), PersistentDataType.STRING) == null)
            event.setCancelled(true);
        }*/
    }
}
