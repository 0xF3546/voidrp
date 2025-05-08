package de.polo.core.zone.services.impl;

import de.polo.api.faction.CharacterRecord;
import de.polo.api.pinwheels.Pinwheel;
import de.polo.api.zone.Region;
import de.polo.core.faction.entity.CoreCharacterRecord;
import de.polo.core.pinwheels.entities.CorePinwheel;
import de.polo.core.utils.Utils;
import de.polo.core.zone.dto.CreateRegionDto;
import de.polo.core.zone.entities.CoreRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.polo.core.Main.database;

public class RegionRepository {
    @Getter
    private final List<Region> regions = new ObjectArrayList<>();

    public RegionRepository() {
        load();
    }

    private void load() {
        database.executeQueryAsync("SELECT * FROM regions")
                .thenApply(result -> {
                    for (Map<String, Object> res : result) {
                        Region region = new CoreRegion(
                                (int) res.get("id"),
                                (String) res.get("name"),
                                Utils.getLocation(
                                        (int) res.get("lcX"),
                                        (int) res.get("lcY"),
                                        (int) res.get("lcZ")
                                ),
                                Utils.getLocation(
                                        (int) res.get("ucX"),
                                        (int) res.get("ucY"),
                                        (int) res.get("ucZ")
                                )
                        );
                        regions.add(region);
                    }
                    return null;
                });
    }

    public Region getRegion(String name) {
        return regions.stream()
                .filter(region -> region.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Region getRegionById(int id) {
        return regions.stream()
                .filter(region -> region.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public int addRegion(CreateRegionDto region) {
        database.insertAndGetKeyAsync(
                "INSERT INTO regions (name, lcX, lcY, lcZ, ucX, ucY, ucZ) VALUES (?, ?, ?, ?, ?, ?, ?)",
                region.name(),
                region.location1().getX(),
                region.location1().getY(),
                region.location1().getZ(),
                region.location2().getX(),
                region.location2().getY(),
                region.location2().getZ()
        ).thenApply(key -> {
            int id = 0;
            if (key.isPresent()) {
                id = key.get();
                Region reg = new CoreRegion(id, region.name(), region.location1(), region.location2());
                regions.add(reg);
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
        return 0;
    }
}
