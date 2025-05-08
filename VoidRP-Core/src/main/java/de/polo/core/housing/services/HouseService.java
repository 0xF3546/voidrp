package de.polo.core.housing.services;

import de.polo.core.game.base.housing.House;
import de.polo.core.player.entities.PlayerData;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HouseService {
    /**
     * Gets a house by its number
     */
    House getHouse(int houseNumber);

    /**
     * Adds a new house to the system
     */
    void addHouse(House house);

    /**
     * Checks if a player is the owner of a specific house
     */
    boolean isPlayerOwner(Player player, int number);

    /**
     * Checks if a player can interact with a house (owner or renter)
     */
    boolean canPlayerInteract(Player player, int number);

    /**
     * Gets a string representation of houses a player has access to
     */
    String getHouseAccessAsString(PlayerData playerData);

    /**
     * Updates the renter information for a house
     */
    void updateRenter(int number) throws Exception;

    /**
     * Gets all houses a player has access to
     */
    List<House> getAccessedHousing(Player player);

    /**
     * Finds the nearest house within a specified range
     */
    House getNearestHouse(Location loc, int range);

    /**
     * Gets all houses owned by a player
     */
    Collection<House> getHouses(Player player);
    Collection<House> getHouses();

    /**
     * Adds a house slot to a player's capacity
     */
    void addHouseSlot(Player player) throws Exception;

    /**
     * Resets a house (removes owner)
     */
    boolean resetHouse(int house) throws Exception;

    /**
     * Opens the house server room menu for a player
     */
    void openHouseServerRoom(Player player, House house);

    /**
     * Performs a cryptocurrency mining tick for all houses
     */
    void doCryptoTick();

    /**
     * Opens the cooking menu for a house
     */
    void openCookMenu(Player player, House house);

    /**
     * Opens the gun cabinet menu for a house
     */
    void openGunCabinet(Player player, House house);

    /**
     * Opens the house treasury menu
     */
    void openHouseTreasury(Player player, House house);

    /**
     * Sets the number of renter slots for a house
     */
    void setMieterSlot(int houseNumber, int mieter);

    void deleteHouse(int houseNumber);
    void refundHouse(int houseNumber);
    void setHousePrice(int houseNumber, int price);
    void updateSign(House house);
}
