package de.polo.core.elevators.services;

import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import de.polo.core.elevators.dto.CreateElevatorDto;
import de.polo.core.elevators.dto.CreateFloorDto;
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

    int addElevator(CreateElevatorDto elevatorDto);

    int addFloor(CreateFloorDto elevatorDto);

    Elevator getElevator(int id);
}
