package de.polo.core.location.services;

import de.polo.api.player.VoidPlayer;
import de.polo.core.storage.GasStationData;
import de.polo.core.storage.LocationData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface LocationService {
    /**
     * Sets a new location in the database
     */
    void setLocation(String name, Player player);

    /**
     * Teleports a player to a named location
     */
    void useLocation(Player player, String name);

    /**
     * Gets a Location object by name
     */
    Location getLocation(String name);

    /**
     * Calculates the distance between a player and a named location
     */
    double getDistanceBetweenCoords(Player player, String name);

    /**
     * Calculates the distance between a VoidPlayer and a named location
     */
    double getDistanceBetweenCoords(VoidPlayer player, String name);

    /**
     * Checks if a player is near a shop and returns its ID
     */
    int isNearShop(Player player);

    /**
     * Gets the shop name by its ID
     */
    String getShopNameById(Integer id);

    /**
     * Checks if a player is near their own house and returns the house ID
     */
    Integer isPlayerNearOwnHouse(Player player);

    /**
     * Checks if a player is near a gas station and returns its ID
     */
    Integer isPlayerGasStation(Player player);

    /**
     * Gets the gas station data if a player is within radius
     */
    GasStationData getGasStationInRadius(Player player);

    /**
     * Gets the name of the nearest location to a player
     */
    String getNearestLocation(Player player);

    /**
     * Gets the ID of the nearest location to a player
     */
    Integer getNearestLocationId(Player player);

    /**
     * Checks if a player is near a farming spot within a given radius
     */
    String isNearFarmingSpot(Player player, int radius);

    /**
     * Gets all loaded locations
     */
    Collection<LocationData> getLocations();

    /**
     * Checks if two locations are equal based on coordinates
     */
    boolean isLocationEqual(Location first, Location second);
}
