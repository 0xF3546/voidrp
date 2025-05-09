package de.polo.core.laboratory.services.impl;

import de.polo.api.laboratory.Laboratory;
import de.polo.core.laboratory.dto.CreateLaboratoryDto;
import de.polo.core.laboratory.services.LaboratoryService;
import de.polo.core.utils.Service;
import org.bukkit.Location;

import java.util.List;

@Service
public class CoreLaboratory implements LaboratoryService {
    private final LaboratoryRepository laboratoryRepository;

    public CoreLaboratory() {
        this.laboratoryRepository = new LaboratoryRepository();
    }

    @Override
    public List<Laboratory> getLaboratories() {
        return laboratoryRepository.getLaboratories();
    }

    @Override
    public Laboratory getNearestLaboratory(Location location, int range) {
        return getLaboratories().stream()
                .filter(laboratory -> laboratory.getLocation().getWorld().equals(location.getWorld()))
                .filter(laboratory -> laboratory.getLocation().distance(location) <= range)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int addLaboratory(CreateLaboratoryDto laboratory) {
        return laboratoryRepository.addLaboratory(laboratory);
    }
}
