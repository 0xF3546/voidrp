package de.polo.core.elevators.services.impl;

import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import de.polo.core.elevators.services.ElevatorService;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreElevatorService implements ElevatorService {
    private final List<Elevator> elevators = new ObjectArrayList<>();
    @Override
    public List<Elevator> getElevators() {
        return elevators;
    }

    @Override
    public Elevator getNearestElevator(Location location, int range) {
        return elevators.stream()
                .filter(elevator -> elevator.floors().stream()
                        .anyMatch(floor -> floor.location().getWorld().equals(location.getWorld()) &&
                                floor.location().distance(location) < range))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Floor getNearestFloor(Location location, int range) {
        return elevators.stream()
                .flatMap(elevator -> elevator.floors().stream())
                .filter(floor -> floor.location().getWorld().equals(location.getWorld()) &&
                        floor.location().distance(location) < range)
                .findFirst()
                .orElse(null);
    }
}
