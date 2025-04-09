package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.utils.Event;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

@Event
public class ArmorStandExitListener implements Listener {
    public ArmorStandExitListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        Entity dismounted = event.getDismounted();
        if (dismounted instanceof ArmorStand armorStand) {
            if (event.getEntity() instanceof Player player) {
                if (armorStand.getCustomName() != null && armorStand.getCustomName().equals("CarryStand_" + player.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }
            armorStand.remove();
        }
    }
}
