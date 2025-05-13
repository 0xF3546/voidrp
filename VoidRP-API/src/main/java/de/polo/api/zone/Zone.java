package de.polo.api.zone;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.enums.ZoneType;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Zone {
    String getName();

    Region getRegion();

    List<VoidPlayer> getPlayersInZone();

    void addPlayer(VoidPlayer player);

    void removePlayer(VoidPlayer player);

    ZoneType getType();
}
