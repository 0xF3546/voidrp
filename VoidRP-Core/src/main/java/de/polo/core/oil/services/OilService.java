package de.polo.core.oil.services;

import de.polo.api.oil.OilPump;
import de.polo.core.oil.dto.CreateOilPumpDto;
import org.bukkit.Location;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface OilService {
    List<OilPump> getOilPumps();
    OilPump getOilPump(int id);
    int addOilPump(CreateOilPumpDto createOilPumpDto);
    OilPump getNearestOilPump(Location location, int range);
    void updateOilPump(OilPump oilPump);
}
