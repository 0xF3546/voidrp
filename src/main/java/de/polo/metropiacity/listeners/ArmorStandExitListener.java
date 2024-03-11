package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class ArmorStandExitListener implements Listener {
    public ArmorStandExitListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onVehicle(EntityDismountEvent event) {
        Player player = (Player) event.getEntity();
        if (event.getDismounted() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getDismounted();
            if (!armorStand.isValid()) {
                return;
            }
            armorStand.remove();
        }
    }
}
