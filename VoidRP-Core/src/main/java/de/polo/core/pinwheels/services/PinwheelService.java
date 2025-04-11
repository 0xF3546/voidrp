package de.polo.core.pinwheels.services;

import de.polo.api.pinwheels.Pinwheel;
import de.polo.core.pinwheels.dto.CreatePinwheelDto;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PinwheelService {
    List<Pinwheel> getPinwheels();
    int addPinwheel(CreatePinwheelDto pinwheel);
}
