package de.polo.core.zone.services.impl;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Zone;
import de.polo.api.zone.enums.ZoneType;
import de.polo.core.utils.Service;
import de.polo.core.zone.services.ZoneService;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreZoneService implements ZoneService {
    private final ZoneRepository repository;

    public CoreZoneService() {
        repository = new ZoneRepository();
    }

    @Override
    public List<Zone> getZones() {
        return repository.getZones();
    }

    @Override
    public List<Zone> getZonesByType(ZoneType type) {
        return getZones()
                .stream()
                .filter(x -> x.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public Zone getZone(String name) {
        return getZones().stream()
                .filter(zone -> zone.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Zone getZoneOfPlayer(VoidPlayer player) {
        return getZones().stream()
                .filter(zone -> zone.getPlayersInZone().contains(player))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addZone(Zone zone) {
        if (!getZones().contains(zone)) {
            getZones().add(zone);
        }
    }
}
