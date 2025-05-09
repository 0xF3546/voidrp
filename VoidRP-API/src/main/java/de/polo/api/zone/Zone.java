package de.polo.api.zone;

import de.polo.api.player.VoidPlayer;
import org.bukkit.Location;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Zone {
    String getName();
    int getRange();
    Region getRegion();
    List<VoidPlayer> getPlayersInZone();
    void addPlayer(VoidPlayer player);
    void removePlayer(VoidPlayer player);
}
