package de.polo.api.elevators;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Elevator {
    List<Floor> floors();
    Floor getFloor(int floorNumber);
    String name();
}
