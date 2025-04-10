package de.polo.core.elevators.services;

import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import org.bukkit.Location;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ElevatorService {
    List<Elevator> getElevators();
    Elevator getNearestElevator(Location location, int range);
    Floor getNearestFloor(Location location, int range);
}
