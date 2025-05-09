package de.polo.core.laboratory.services.impl;

import de.polo.api.VoidAPI;
import de.polo.api.laboratory.Laboratory;
import de.polo.api.laboratory.enums.LaboratoryType;
import de.polo.core.faction.service.FactionService;
import de.polo.core.laboratory.dto.CreateLaboratoryDto;
import de.polo.core.laboratory.entities.CoreLaboratory;
import de.polo.core.location.services.LocationService;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.sql.PreparedStatement;
import java.util.List;

import static de.polo.core.Main.database;

public class LaboratoryRepository {
    private final List<Laboratory> laboratories = new ObjectArrayList<>();

    public List<Laboratory> getLaboratories() {
        if (laboratories.isEmpty()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM laboratories")) {
                var resultSet = statement.executeQuery();
                FactionService factionService = VoidAPI.getService(FactionService.class);
                while (resultSet.next()) {
                    var id = resultSet.getInt("id");
                    int location = resultSet.getInt("locationId");
                    var name = resultSet.getString("name");
                    var type = LaboratoryType.valueOf(resultSet.getString("type"));
                    CoreLaboratory laboratory = new CoreLaboratory(id, type, locationService.getLocation(location), name);
                    var factionId = resultSet.getInt("factionId");
                    if (factionId != 0) {
                        var faction = factionService.getById(factionId);
                        if (faction != null) {
                            laboratory.setFaction(faction);
                        }
                    }
                    var lastAttack = resultSet.getTimestamp("lastAttack");
                    if (lastAttack != null) {
                        laboratory.setLastAttack(lastAttack.toLocalDateTime());
                    }
                    laboratories.add(laboratory);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return laboratories;
    }

    public int addLaboratory(CreateLaboratoryDto createLaboratoryDto) {
        var locationService = VoidAPI.getService(LocationService.class);
        int locationDbId = locationService.createLocation("laboratory_" + createLaboratoryDto.name(), createLaboratoryDto.location());
        database.insertAndGetKeyAsync(
                "INSERT INTO laboratories (locationId, name) VALUES (?, ?)",
                locationDbId,
                createLaboratoryDto.name()
        ).thenApply(key -> {
            int id = 0;
            if (key.isPresent()) {
                id = key.get();
                Laboratory laboratory = new CoreLaboratory(id, LaboratoryType.DRUG_LAB, createLaboratoryDto.location(), createLaboratoryDto.name());
                laboratories.add(laboratory);
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return 0;
    }

    public void updateLaboratory(Laboratory laboratory) {
        database.updateAsync(
                "UPDATE laboratories SET factionId = ?, lastAttack = ? WHERE id = ?",
                laboratory.getFaction() != null ? laboratory.getFaction().getId() : null,
                laboratory.getLastAttack(),
                laboratory.getId()
        ).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }
}
