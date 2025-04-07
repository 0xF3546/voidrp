package de.polo.voidroleplay.location.services;

import de.polo.voidroleplay.storage.NaviData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface NavigationService {
    /**
     * Gets the nearest navigation point to a given location
     */
    NaviData getNearestNaviPoint(Location location);

    /**
     * Opens the navigation GUI for a player
     */
    void openNavi(Player player, String search);

    /**
     * Creates a navigation route to specific coordinates
     */
    void createNaviByCord(Player player, int x, int y, int z);

    /**
     * Creates a navigation route to a named location
     */
    void createNavi(Player player, String nav, boolean silent);

    /**
     * Creates a navigation route by location name
     */
    void createNaviByLocation(Player player, String nav);
}
