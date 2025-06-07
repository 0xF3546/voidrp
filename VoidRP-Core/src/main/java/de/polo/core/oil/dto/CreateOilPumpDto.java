package de.polo.core.oil.dto;

import lombok.Getter;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public record CreateOilPumpDto(@Getter Location location) {
}
