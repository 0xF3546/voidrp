package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class ExpPickupListener implements Listener {
    public ExpPickupListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent ev){
        Entity e = ev.getEntity();
        if(e instanceof ExperienceOrb){
            ev.setCancelled(true);
            ev.setTarget(null);
        }
    }

}
