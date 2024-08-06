package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

public class ArmorStandExitListener implements Listener {
    public ArmorStandExitListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onVehicle(EntityDismountEvent event) {
        Entity dismounted = event.getDismounted(); // Entität, von der ausgestiegen wurde
        if (dismounted instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) dismounted;
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (armorStand.getCustomName() != null && armorStand.getCustomName().equals("CarryStand_" + player.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }
            armorStand.remove();
        }
    }
}
