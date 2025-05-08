package de.polo.core.zone.services.impl;

import de.polo.api.zone.Region;
import de.polo.core.utils.Service;
import de.polo.core.zone.dto.CreateRegionDto;
import de.polo.core.zone.services.RegionService;

import java.util.List;

@Service
public class CoreRegionService implements RegionService {
    private final RegionRepository repository;

    public CoreRegionService() {
        this.repository = new RegionRepository();
    }
    @Override
    public List<Region> getRegions() {
        return repository.getRegions();
    }

    @Override
    public Region getRegion(String name) {
        return repository.getRegion(name);
    }

    @Override
    public Region getRegionById(int id) {
        return repository.getRegionById(id);
    }

    @Override
    public int createRegion(CreateRegionDto regionDto) {
        return repository.addRegion(regionDto);
    }
}
