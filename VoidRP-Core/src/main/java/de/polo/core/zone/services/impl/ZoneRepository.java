package de.polo.core.zone.services.impl;

import de.polo.api.VoidAPI;
import de.polo.api.zone.Zone;
import de.polo.api.zone.enums.ZoneType;
import de.polo.core.zone.entities.CoreZone;
import de.polo.core.zone.services.RegionService;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.sql.Connection;
import java.util.List;

import static de.polo.core.Main.database;

public class ZoneRepository {
    @Getter
    private final List<Zone> zones = new ObjectArrayList<>();

    public ZoneRepository() {
        load();
    }

    private void load() {
        try (Connection connection = database.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM zones");
            var resultSet = statement.executeQuery();
            RegionService regionService = VoidAPI.getService(RegionService.class);
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Zone zone = new CoreZone(name,
                        regionService.getRegionById(resultSet.getInt("regionId")),
                        ZoneType.valueOf(resultSet.getString("type")));
                zones.add(zone);
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
