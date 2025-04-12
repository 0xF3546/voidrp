package de.polo.core.elevators.dto;

import de.polo.api.elevators.Elevator;
import lombok.Getter;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public record CreateFloorDto(@Getter Elevator elevator, @Getter Location location, @Getter int floor) {
}
