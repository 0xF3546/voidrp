package de.polo.core.elevators;

import de.polo.api.VoidAPI;
import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import de.polo.api.zone.EnterZoneEvent;
import de.polo.core.elevators.entities.ElevatorGUI;
import de.polo.core.elevators.services.ElevatorService;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class EnterElevatorListener implements Listener {
    @EventHandler
    public void onElevatorEnter(EnterZoneEvent event) {
        if (event.getZone().getName().equalsIgnoreCase("elevator")) {
            ElevatorService elevatorService = VoidAPI.getService(ElevatorService.class);
            Floor elevator = elevatorService.getNearestFloor(event.getPlayer().getPlayer().getLocation(), 10);
            if (elevator == null) return;
            new ElevatorGUI(event.getPlayer(), elevator.elevator(), elevator).open();
        }
    }
}
