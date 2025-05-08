package de.polo.core.laboratory.services;

import de.polo.api.laboratory.Laboratory;
import de.polo.core.laboratory.dto.CreateLaboratoryDto;
import org.bukkit.Location;

import java.util.List;

public interface LaboratoryService {
    List<Laboratory> getLaboratories();
    Laboratory getNearestLaboratory(Location location, int range);
    int addLaboratory(CreateLaboratoryDto laboratory);
}
