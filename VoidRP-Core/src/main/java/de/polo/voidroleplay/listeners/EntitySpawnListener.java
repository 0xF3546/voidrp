package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {
    public EntitySpawnListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

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
