package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.DataStorage.PlayerVehicleData;
import de.polo.void_roleplay.DataStorage.VehicleData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Vehicles {
    public static Map<String, VehicleData> vehicleDataMap = new HashMap<>();
    public static Map<Integer, PlayerVehicleData> playerVehicleDataMap = new HashMap<Integer, PlayerVehicleData>();
    public static HashMap<String, Integer> vehicleIDByUUid = new HashMap<>();
    public static void loadVehicles() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `vehicles`");
        while (result.next()) {
            VehicleData vehicleData = new VehicleData();
            vehicleData.setId(result.getInt(1));
            vehicleData.setName(result.getString(2));
            vehicleData.setAcceleration(result.getFloat(3));
            vehicleData.setMaxspeed(result.getInt(4));
            vehicleData.setPrice(result.getInt(5));
            vehicleData.setMaxFuel(result.getInt(6));
            vehicleDataMap.put(result.getString(2), vehicleData);
        }
    }

    public static void loadPlayerVehicles() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `player_vehicles`");
        while (result.next()) {
            PlayerVehicleData playerVehicleData = new PlayerVehicleData();
            playerVehicleData.setId(result.getInt(1));
            playerVehicleData.setUuid(result.getString(2));
            playerVehicleData.setType(result.getString(3));
            playerVehicleData.setKm(result.getInt(4));
            playerVehicleData.setFuel(result.getFloat(5));
            playerVehicleData.setParked(result.getBoolean(6));
            playerVehicleData.setX(result.getInt(7));
            playerVehicleData.setY(result.getInt(8));
            playerVehicleData.setZ(result.getInt(9));
            playerVehicleData.setWelt(Bukkit.getWorld(result.getString(10)));
            playerVehicleData.setYaw(result.getFloat(11));
            playerVehicleData.setPitch(result.getFloat(12));
            playerVehicleDataMap.put(result.getInt(1), playerVehicleData);
            vehicleIDByUUid.put(result.getString(2), result.getInt(1));
        }
    }

    public static void giveVehicle(Player player, String vehicle) throws SQLException {
        VehicleData vehicleData = vehicleDataMap.get(vehicle);
        assert vehicleData != null;
        Statement statement = MySQL.getStatement();
        statement.execute("INSERT INTO `player_vehicles` (`uuid`, `type`) VALUES ('" + player.getUniqueId().toString() + "', '" + vehicleData.getName() + "')");
        ResultSet result = statement.executeQuery("SELECT LAST_INSERT_ID()");
        LocationManager.useLocation(player, "vehicleshop_out");
        if (result.next()) {
            PlayerVehicleData playerVehicleData = new PlayerVehicleData();
            playerVehicleData.setId(result.getInt(1));
            playerVehicleData.setUuid(player.getUniqueId().toString());
            playerVehicleData.setType(vehicle);
            playerVehicleData.setKm(0);
            playerVehicleData.setFuel(vehicleData.getMaxFuel());
            playerVehicleData.setParked(false);
            playerVehicleData.setX((int) player.getLocation().getX());
            playerVehicleData.setY((int) player.getLocation().getY());
            playerVehicleData.setZ((int) player.getLocation().getZ());
            playerVehicleData.setWelt(player.getWorld());
            playerVehicleData.setYaw(player.getLocation().getYaw());
            playerVehicleData.setPitch(player.getLocation().getPitch());
            playerVehicleDataMap.put(result.getInt(1), playerVehicleData);
            spawnVehicle(player, playerVehicleData);
        }
    }

    public static void spawnPlayerVehicles(Player player) {
        for (PlayerVehicleData playerVehicleData : playerVehicleDataMap.values()) {
            if (playerVehicleData.getUuid().equals(player.getUniqueId().toString())) {
                spawnVehicle(player, playerVehicleData);
            }
        }
    }

    public static void spawnVehicle(Player player, PlayerVehicleData playerVehicleData) {
        Location location = new Location(playerVehicleData.getWelt(), playerVehicleData.getX(), playerVehicleData.getY(), playerVehicleData.getZ(), (float) playerVehicleData.getYaw(), (float) playerVehicleData.getPitch());
        Minecart minecart = (Minecart) playerVehicleData.getWelt().spawnEntity(location, EntityType.MINECART);
        NamespacedKey key_id = new NamespacedKey(Main.plugin, "id");
        minecart.getPersistentDataContainer().set(key_id, PersistentDataType.INTEGER, playerVehicleData.getId());
        NamespacedKey key_uuid = new NamespacedKey(Main.plugin, "uuid");
        minecart.getPersistentDataContainer().set(key_id, PersistentDataType.STRING, playerVehicleData.getUuid());
        NamespacedKey key_km = new NamespacedKey(Main.plugin, "km");
        minecart.getPersistentDataContainer().set(key_km, PersistentDataType.INTEGER, playerVehicleData.getKm());
        NamespacedKey key_fuel = new NamespacedKey(Main.plugin, "fuel");
        minecart.getPersistentDataContainer().set(key_fuel, PersistentDataType.FLOAT, playerVehicleData.getFuel());
        NamespacedKey key_lock = new NamespacedKey(Main.plugin, "lock");
        minecart.getPersistentDataContainer().set(key_lock, PersistentDataType.INTEGER, 1);
    }

    public static void deleteVehicleById(Integer id) throws SQLException {
        for (Entity entity : Bukkit.getWorld("ciddy").getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                NamespacedKey key_id = new NamespacedKey(Main.plugin, "id");
                if (Objects.equals(entity.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER), id)) {
                    Statement statement = MySQL.getStatement();
                    int km = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                    float fuel = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                    statement.executeUpdate("UPDATE `player_vehicles` SET `km` = " + km + ", `fuel` = " + fuel + ", `x` = " + entity.getLocation().getX() + ", `y` = " + entity.getLocation().getY() + ", `z` = " + entity.getLocation().getZ() + ", `welt` = " + entity.getLocation().getWorld().toString() + ", `yaw` = " + entity.getLocation().getYaw() + ", `pitch` = " + entity.getLocation().getPitch() + " WHERE `id` = " + id);
                    entity.remove();
                }
            }
        }
    }

    public static void deleteVehicleByUUID(String uuid) throws SQLException {
        for (Entity entity : Bukkit.getWorld("ciddy").getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                NamespacedKey key_uuid = new NamespacedKey(Main.plugin, "uuid");
                if (Objects.equals(entity.getPersistentDataContainer().get(key_uuid, PersistentDataType.STRING), uuid)) {
                    Statement statement = MySQL.getStatement();
                    int id = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                    int km = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                    float fuel = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                    statement.executeUpdate("UPDATE `player_vehicles` SET `km` = " + km + ", `fuel` = " + fuel + ", `x` = " + entity.getLocation().getX() + ", `y` = " + entity.getLocation().getY() + ", `z` = " + entity.getLocation().getZ() + ", `welt` = " + entity.getLocation().getWorld().toString() + ", `yaw` = " + entity.getLocation().getYaw() + ", `pitch` = " + entity.getLocation().getPitch() + " WHERE `id` = " + id);
                    entity.remove();
                }
            }
        }
    }
}
