package de.polo.core.pinwheels.services.impl;

import de.polo.api.VoidAPI;
import de.polo.api.pinwheels.Pinwheel;
import de.polo.core.location.services.LocationService;
import de.polo.core.pinwheels.dto.CreatePinwheelDto;
import de.polo.core.pinwheels.entities.CorePinwheel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PinwheelRepository {
    private final List<Pinwheel> pinwheels = new ObjectArrayList<>();

    public List<Pinwheel> getPinwheels() {
        if (pinwheels.isEmpty()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM pinwheels")) {
                var resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    var id = resultSet.getInt("id");
                    int location = resultSet.getInt("locationId");
                    var name = resultSet.getString("name");
                    pinwheels.add(new CorePinwheel(id, locationService.getLocation(location), name));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pinwheels;
    }

    public int addPinwheel(CreatePinwheelDto createPinwheelDto) {
        var locationService = VoidAPI.getService(LocationService.class);
        int locationDbId = locationService.createLocation("pinwheel_" + createPinwheelDto.name(), createPinwheelDto.location());
        database.insertAndGetKeyAsync(
                "INSERT INTO pinwheels (locationId, name) VALUES (?, ?)",
                locationDbId,
                createPinwheelDto.name()
        ).thenApply(key -> {
            int id = 0;
            if (key.isPresent()) {
                id = key.get();
                Pinwheel pinwheel = new CorePinwheel(id, createPinwheelDto.location(), createPinwheelDto.name());
                pinwheels.add(pinwheel);
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return 0;
    }
}
