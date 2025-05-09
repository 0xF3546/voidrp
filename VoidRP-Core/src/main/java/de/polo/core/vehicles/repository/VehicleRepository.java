package de.polo.core.vehicles.repository;

import de.polo.core.game.base.vehicle.PlayerVehicleData;
import de.polo.core.game.base.vehicle.VehicleData;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.polo.core.Main.database;

/**
 * Repository for handling database operations related to vehicles.
 */
public class VehicleRepository {

    /**
     * Loads all vehicle types from the database.
     *
     * @return a map of vehicle names to their data
     * @throws SQLException if the query fails
     */
    public Map<String, VehicleData> loadVehicles() throws SQLException {
        Map<String, VehicleData> vehicleDataMap = new HashMap<>();
        try (Statement statement = database.getConnection().createStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM `vehicles`")) {
            while (result.next()) {
                VehicleData vehicleData = new VehicleData();
                vehicleData.setId(result.getInt(1));
                vehicleData.setName(result.getString(2));
                vehicleData.setAcceleration(result.getFloat(3));
                vehicleData.setMaxspeed(result.getInt(4));
                vehicleData.setPrice(result.getInt(5));
                vehicleData.setMaxFuel(result.getInt(6));
                vehicleData.setTax(result.getInt(7));
                vehicleDataMap.put(result.getString(2), vehicleData);
            }
        }
        return vehicleDataMap;
    }

    /**
     * Loads all player vehicles from the database.
     *
     * @return a map of vehicle IDs to their player vehicle data
     * @throws SQLException if the query fails
     */
    public Map<Integer, PlayerVehicleData> loadPlayerVehicles() throws SQLException {
        Map<Integer, PlayerVehicleData> playerVehicleDataMap = new HashMap<>();
        Map<String, Integer> vehicleIDByUuid = new HashMap<>();
        try (Statement statement = database.getConnection().createStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM `player_vehicles`")) {
            while (result.next()) {
                PlayerVehicleData playerVehicleData = new PlayerVehicleData();
                playerVehicleData.setId(result.getInt("id"));
                playerVehicleData.setUuid(result.getString("uuid"));
                playerVehicleData.setType(result.getString("type"));
                playerVehicleData.setKm(result.getInt("km"));
                playerVehicleData.setFuel(result.getFloat("fuel"));
                playerVehicleData.setX(result.getInt("x"));
                playerVehicleData.setY(result.getInt("y"));
                playerVehicleData.setZ(result.getInt("z"));
                playerVehicleData.setWelt(Bukkit.getWorld(result.getString(10)));
                playerVehicleData.setYaw(result.getFloat("yaw"));
                playerVehicleData.setPitch(result.getFloat("pitch"));
                playerVehicleData.setFactionId(result.getInt("factionId"));
                playerVehicleDataMap.put(result.getInt(1), playerVehicleData);
                vehicleIDByUuid.put(result.getString(2), result.getInt(1));
            }
        }
        return playerVehicleDataMap;
    }

    /**
     * Saves a new player vehicle to the database and returns its ID.
     *
     * @param uuid        the UUID of the player
     * @param vehicleName the name of the vehicle type
     * @return the ID of the newly created vehicle
     * @throws SQLException if the operation fails
     */
    public int savePlayerVehicle(UUID uuid, String vehicleName) throws SQLException {
        String sql = "INSERT INTO `player_vehicles` (`uuid`, `type`) VALUES (?, ?)";
        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, vehicleName);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to retrieve generated vehicle ID");
            }
        }
    }

    /**
     * Deletes a player vehicle from the database.
     *
     * @param vehicleId the ID of the vehicle to delete
     * @throws SQLException if the operation fails
     */
    public void deletePlayerVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM player_vehicles WHERE id = ?";
        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, vehicleId);
            stmt.executeUpdate();
        }
    }
}