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
                // Überprüfe, ob der ArmorStand den richtigen Namen hat
                if (armorStand.getCustomName() != null && armorStand.getCustomName().equals("CarryStand_" + player.getUniqueId())) {
                    // Verhindere das Verlassen des ArmorStands
                    event.setCancelled(true);
                    return;
                }
            }
            // Entferne den ArmorStand, wenn er nicht mehr gültig ist
            if (!armorStand.isValid()) {
                armorStand.remove();
            }
        }
    }
}
