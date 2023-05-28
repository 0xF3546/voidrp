package de.polo.metropiacity.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class expPickupListener implements Listener {
    @EventHandler
    public void onEntityTarget(EntityTargetEvent ev){
        Entity e = ev.getEntity();
        if(e instanceof ExperienceOrb){
            ev.setCancelled(true);
            ev.setTarget(null);
        }
    }

}
