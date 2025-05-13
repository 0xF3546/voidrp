package de.polo.core.zone.services;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Zone;
import de.polo.api.zone.enums.ZoneType;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ZoneService {
    List<Zone> getZones();

    List<Zone> getZonesByType(ZoneType type);

    Zone getZone(String name);

    Zone getZoneOfPlayer(VoidPlayer player);

    void addZone(Zone zone);
}
