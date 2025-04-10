package de.polo.core.elevators.entities;

import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import lombok.Getter;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public record CoreElevator(@Getter List<Floor> floors, @Getter String name) implements Elevator {

    @Override
    public Floor getFloor(int floorNumber) {
        return floors.stream()
                .filter(floor -> floor.floorNumber() == floorNumber)
                .findFirst()
                .orElse(null);
    }
}
