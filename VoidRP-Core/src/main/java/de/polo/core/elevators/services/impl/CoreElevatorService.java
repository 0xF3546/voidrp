package de.polo.core.elevators.services.impl;

import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import de.polo.core.elevators.dto.CreateElevatorDto;
import de.polo.core.elevators.dto.CreateFloorDto;
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
    private final ElevatorRepository elevatorRepository;
    public CoreElevatorService() {
        this.elevatorRepository = new ElevatorRepository();
    }
    @Override
    public List<Elevator> getElevators() {
        return elevatorRepository.getElevators();
    }

    @Override
    public Elevator getNearestElevator(Location location, int range) {
        return elevatorRepository.getElevators().stream()
                .filter(elevator -> elevator.floors().stream()
                        .anyMatch(floor -> floor.location().getWorld().equals(location.getWorld()) &&
                                floor.location().distance(location) < range))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Floor getNearestFloor(Location location, int range) {
        return elevatorRepository.getElevators().stream()
                .flatMap(elevator -> elevator.floors().stream())
                .filter(floor -> floor.location().getWorld().equals(location.getWorld()) &&
                        floor.location().distance(location) < range)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int addElevator(CreateElevatorDto elevatorDto) {
        return elevatorRepository.addElevator(elevatorDto);
    }

    @Override
    public int addFloor(CreateFloorDto elevatorDto) {
        return elevatorRepository.addFloor(elevatorDto);
    }

    @Override
    public Elevator getElevator(int id) {
        return elevatorRepository.getElevators().stream()
                .filter(elevator -> elevator.id() == id)
                .findFirst()
                .orElse(null);
    }
}
