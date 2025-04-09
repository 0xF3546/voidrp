package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

@Event
public class ExplosionListener implements Listener {
    public ExplosionListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }
}
