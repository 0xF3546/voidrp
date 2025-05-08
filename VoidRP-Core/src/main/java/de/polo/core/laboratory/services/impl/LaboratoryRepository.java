package de.polo.core.laboratory.services.impl;

import de.polo.api.VoidAPI;
import de.polo.api.laboratory.Laboratory;
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
                while (resultSet.next()) {
                    var id = resultSet.getInt("id");
                    int location = resultSet.getInt("locationId");
                    var name = resultSet.getString("name");
                    laboratories.add(new CoreLaboratory(id, locationService.getLocation(location), name));
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
                Laboratory laboratory = new CoreLaboratory(id, createLaboratoryDto.location(), createLaboratoryDto.name());
                laboratories.add(laboratory);
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return 0;
    }
}
