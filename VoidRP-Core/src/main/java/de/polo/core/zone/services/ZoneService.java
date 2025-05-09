package de.polo.core.zone.services;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Zone;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ZoneService {
    List<Zone> getZones();

    Zone getZone(String name);

    Zone getZoneOfPlayer(VoidPlayer player);

    void addZone(Zone zone);
}
