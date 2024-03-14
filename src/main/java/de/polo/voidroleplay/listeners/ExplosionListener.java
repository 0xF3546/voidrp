package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionListener implements Listener {
    public ExplosionListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }
}
