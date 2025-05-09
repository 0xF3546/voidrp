package de.polo.core.vehicles.services;

import de.polo.core.game.base.vehicle.PlayerVehicleData;
import de.polo.core.game.base.vehicle.VehicleData;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing vehicles and player vehicle data in the game.
 *
 * @author Mayson1337
 * @version 1.1.0
 * @since 1.0.0
 */
public interface VehicleService {
    /**
     * Retrieves all available vehicle types.
     *
     * @return an unmodifiable map of vehicle names to their data
     */
    Map<String, VehicleData> getVehicles();

    /**
     * Retrieves all player-owned vehicles.
     *
     * @return an unmodifiable map of vehicle IDs to their player vehicle data
     */
    Map<Integer, PlayerVehicleData> getPlayerVehicles();

    /**
     * Retrieves vehicle data by its name.
     *
     * @param name the name of the vehicle
     * @return an Optional containing the vehicle data, or empty if not found
     */
    Optional<VehicleData> getVehicleByName(String name);

    /**
     * Retrieves player vehicle data by its ID.
     *
     * @param id the ID of the player vehicle
     * @return an Optional containing the player vehicle data, or empty if not found
     */
    Optional<PlayerVehicleData> getPlayerVehicleById(Integer id);

    /**
     * Spawns a vehicle for a player at its stored location.
     *
     * @param player            the player who owns the vehicle
     * @param playerVehicleData the player's vehicle data
     * @return the spawned Minecart entity
     * @throws de.polo.core.vehicles.services.exceptions.VehicleServiceException if spawning fails
     */
    Minecart spawnVehicle(Player player, PlayerVehicleData playerVehicleData);

    /**
     * Spawns all vehicles owned by a player.
     *
     * @param player the player whose vehicles should be spawned
     */
    void spawnPlayerVehicles(Player player);

    /**
     * Deletes a vehicle by its ID, removing it from the world and database.
     *
     * @param id the ID of the vehicle to delete
     */
    void deleteVehicleById(Integer id);

    /**
     * Deletes a vehicle by its owner's UUID, removing it from the world and database.
     *
     * @param uuid the UUID of the vehicle owner
     */
    void deleteVehicleByUUID(UUID uuid);

    /**
     * Toggles the lock state of a vehicle.
     *
     * @param id     the ID of the vehicle
     * @param player the player toggling the lock
     */
    void toggleVehicleState(Integer id, Player player);

    /**
     * Refuels a vehicle with a specified amount or to its maximum capacity.
     *
     * @param vehicle the vehicle to refuel
     * @param newFuel the amount of fuel to add, or null to fill to max
     */
    void fillVehicle(Vehicle vehicle, Integer newFuel);

    /**
     * Finds the nearest player vehicle to a given location.
     *
     * @param location the reference location
     * @return an Optional containing the nearest player vehicle data, or empty if none found
     */
    Optional<PlayerVehicleData> getNearestVehicle(Location location);

    /**
     * Assigns a vehicle to a player and spawns it.
     *
     * @param player      the player receiving the vehicle
     * @param vehicleName the name of the vehicle type
     * @throws VehicleServiceException if assignment fails
     */
    void giveVehicle(Player player, String vehicleName);

    /**
     * Removes a vehicle from the database.
     *
     * @param vehicleId the ID of the vehicle to remove
     * @throws VehicleServiceException if removal fails
     */
    void removeVehicleFromDatabase(Integer vehicleId);
}