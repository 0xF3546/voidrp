package de.polo.core.zone.services;

import de.polo.api.zone.Region;
import de.polo.core.zone.dto.CreateRegionDto;

import java.util.List;

public interface RegionService {
    List<Region> getRegions();

    Region getRegion(String name);

    Region getRegionById(int id);

    int createRegion(CreateRegionDto regionDto);
}
