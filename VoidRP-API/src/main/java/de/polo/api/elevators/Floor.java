package de.polo.api.elevators;

import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Floor {
    /**
     * @return the floor number
     */
    int floorNumber();

    /**
     * @return the elevator that is on this floor
     */
    Elevator elevator();

    /**
     * @return the location of the floor
     */
    Location location();
}
