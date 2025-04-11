package de.polo.core.pinwheels.dto;

import lombok.Getter;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public record CreatePinwheelDto(@Getter Location location, @Getter String name) {
}
