package de.polo.core.vehicles.listeners;

import de.polo.core.utils.Event;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class VehicleCrashListener implements Listener {
    @EventHandler
    public static void onCrash(VehicleEntityCollisionEvent event) {
        if (event.getVehicle() instanceof Boat) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                double speedMetersPerSecond = event.getVehicle().getVelocity().length();
                double kmh = speedMetersPerSecond * 36;
                    if (kmh >= 0.1) {
                        player.setVelocity(player.getLocation().getDirection().multiply(-2));
                        player.damage(Math.floor(kmh / 5));
                        // car.crash(Math.floor(kmh * 10));
                    }

            }
        }
    }
}
