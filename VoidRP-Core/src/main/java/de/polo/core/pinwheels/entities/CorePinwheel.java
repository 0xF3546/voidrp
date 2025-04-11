package de.polo.core.pinwheels.entities;

import de.polo.api.pinwheels.Pinwheel;
import lombok.Getter;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public record CorePinwheel(@Getter int id, @Getter Location location, @Getter String name) implements Pinwheel {
}
