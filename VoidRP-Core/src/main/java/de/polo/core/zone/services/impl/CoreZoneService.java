package de.polo.core.zone.services.impl;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Zone;
import de.polo.core.utils.Service;
import de.polo.core.zone.services.ZoneService;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreZoneService implements ZoneService {
    private final List<Zone> zones = new ObjectArrayList<>();

    @Override
    public List<Zone> getZones() {
        return zones;
    }

    @Override
    public Zone getZone(String name) {
        return zones.stream()
                .filter(zone -> zone.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Zone getZoneOfPlayer(VoidPlayer player) {
        return zones.stream()
                .filter(zone -> zone.getPlayersInZone().contains(player))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addZone(Zone zone) {
        if (!zones.contains(zone)) {
            zones.add(zone);
        }
    }
}
