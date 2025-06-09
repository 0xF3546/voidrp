package de.polo.core.oil.services.impl;

import de.polo.api.oil.OilPump;
import de.polo.core.oil.dto.CreateOilPumpDto;
import de.polo.core.oil.services.OilService;
import de.polo.core.utils.Service;
import org.bukkit.Location;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreOilService implements OilService {
    private final OilRepository repository;
    public CoreOilService() {
        this.repository = new OilRepository();
    }
    @Override
    public List<OilPump> getOilPumps() {
        return repository.getOilPumps();
    }

    @Override
    public OilPump getOilPump(int id) {
        return getOilPumps()
                .stream()
                .filter(oilPump -> oilPump.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int addOilPump(CreateOilPumpDto createOilPumpDto) {
        return repository.addOilPump(createOilPumpDto);
    }

    @Override
    public OilPump getNearestOilPump(Location location, int range) {
        return getOilPumps()
                .stream()
                .filter(oilPump -> oilPump.getLocation().distance(location) <= range)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateOilPump(OilPump oilPump) {
        if (oilPump == null) return;
        repository.updateOilPump(oilPump);
    }
}
