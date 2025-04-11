package de.polo.core.pinwheels.services.impl;

import de.polo.api.pinwheels.Pinwheel;
import de.polo.core.pinwheels.dto.CreatePinwheelDto;
import de.polo.core.pinwheels.services.PinwheelService;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CorePinwheelService implements PinwheelService {
    private final PinwheelRepository pinwheelRepository;
    public CorePinwheelService() {
        this.pinwheelRepository = new PinwheelRepository();
    }
    @Override
    public List<Pinwheel> getPinwheels() {
        return pinwheelRepository.getPinwheels();
    }

    @Override
    public int addPinwheel(CreatePinwheelDto pinwheel) {
        return pinwheelRepository.addPinwheel(pinwheel);
    }
}
