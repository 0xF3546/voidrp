package de.polo.core.oil.services.impl;

import de.polo.api.VoidAPI;
import de.polo.api.oil.OilPump;
import de.polo.core.Main;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.CompanyManager;
import de.polo.core.oil.dto.CreateOilPumpDto;
import de.polo.core.oil.entities.CoreOilPump;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class OilRepository {
    private final List<OilPump> oilPumps = new ObjectArrayList<>();

    public List<OilPump> getOilPumps() {
        if (oilPumps.isEmpty()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            try (PreparedStatement statement = database.getConnection().prepareStatement("SELECT * FROM oilpumps")) {
                var resultSet = statement.executeQuery();
                CompanyManager companyManager = Main.companyManager;
                while (resultSet.next()) {
                    var id = resultSet.getInt("id");
                    int location = resultSet.getInt("locationId");
                    CoreOilPump oilPump = new CoreOilPump(id, null, locationService.getLocation(location), resultSet.getInt("level"), resultSet.getInt("oil"));
                    var companyId = resultSet.getInt("companyId");
                    if (companyId != 0) {
                        var company = companyManager.getCompanyById(companyId);
                        if (company != null) {
                            oilPump.setCompany(company);
                        }
                    }
                    oilPumps.add(oilPump);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return oilPumps;
    }

    public int addOilPump(CreateOilPumpDto createOilPumpDto) {
        var locationService = VoidAPI.getService(LocationService.class);
        int locationDbId = locationService.createLocation("oilpump_" + UUID.randomUUID(), createOilPumpDto.location());
        database.insertAndGetKeyAsync(
                "INSERT INTO oilpumps (locationId, name) VALUES (?, ?)",
                locationDbId
        ).thenApply(key -> {
            int id = 0;
            if (key.isPresent()) {
                id = key.get();
                OilPump oilPump = new CoreOilPump(id, null, createOilPumpDto.location(), 0, 0);
                oilPumps.add(oilPump);
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return 0;
    }

    public void updateOilPump(OilPump oilPump) {
        database.updateAsync(
                "UPDATE oilpump SET companyId = ?, level = ?, oil = ? WHERE id = ?",
                oilPump.getCompany() != null ? oilPump.getCompany().getId() : null,
                oilPump.getLevel(),
                oilPump.getOil(),
                oilPump.getId()
        ).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }
}
