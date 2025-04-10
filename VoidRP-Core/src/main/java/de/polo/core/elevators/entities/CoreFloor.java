package de.polo.core.elevators.entities;

import de.polo.api.elevators.Elevator;
import de.polo.api.elevators.Floor;
import lombok.Getter;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public record CoreFloor(@Getter Elevator elevator, @Getter int floorNumber,
                        @Getter Location location) implements Floor {
}
