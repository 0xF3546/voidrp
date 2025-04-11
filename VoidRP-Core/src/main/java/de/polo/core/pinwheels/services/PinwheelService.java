package de.polo.core.pinwheels.services;

import de.polo.api.pinwheels.Pinwheel;
import de.polo.core.pinwheels.dto.CreatePinwheelDto;
import org.bukkit.Location;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PinwheelService {
    List<Pinwheel> getPinwheels();
    Pinwheel getNearestPinwheel(Location location, int range);
    int addPinwheel(CreatePinwheelDto pinwheel);
}
