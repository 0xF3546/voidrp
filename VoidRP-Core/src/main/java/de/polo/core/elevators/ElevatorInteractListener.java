package de.polo.core.elevators;

import de.polo.api.VoidAPI;
import de.polo.api.elevators.Floor;
import de.polo.core.elevators.services.ElevatorService;
import de.polo.core.utils.Event;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class ElevatorInteractListener implements Listener {
    @EventHandler
    public void onPinwheelInteractDoor(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.STONE_BUTTON) return;
        ElevatorService elevatorService = VoidAPI.getService(ElevatorService.class);
        Floor floor = elevatorService.getNearestFloor(event.getClickedBlock().getLocation(), 5);
        if (floor == null) return;
        new ElevatorGUI(VoidAPI.getPlayer(event.getPlayer()), floor).open();
    }
}
