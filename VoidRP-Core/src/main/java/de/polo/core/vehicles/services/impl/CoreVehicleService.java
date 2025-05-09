package de.polo.core.vehicles.services.impl;

import de.polo.core.Main;
import de.polo.core.game.base.vehicle.PlayerVehicleData;
import de.polo.core.game.base.vehicle.VehicleData;
import de.polo.core.utils.Service;
import de.polo.core.vehicles.repository.VehicleRepository;
import de.polo.core.vehicles.services.VehicleService;
import de.polo.core.vehicles.services.exceptions.VehicleServiceException;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the VehicleService interface for managing vehicles and player vehicle data.
 *
 * @author Mayson1337
 * @version 1.1.0
 * @since 1.0.0
 */
@Service
public class CoreVehicleService implements VehicleService {
    private static final String WORLD_NAME = "world";
    private final VehicleRepository vehicleRepository;
    private final Map<String, VehicleData> vehicleDataMap;
    private final Map<Integer, PlayerVehicleData> playerVehicleDataMap;
    private final Map<String, Integer> vehicleIDByUuid;
    private final NamespacedKey keyId;
    private final NamespacedKey keyUuid;
    private final NamespacedKey keyKm;
    private final NamespacedKey keyFuel;
    private final NamespacedKey keyLock;
    private final NamespacedKey keyType;

    public CoreVehicleService() {
        this.vehicleRepository = new VehicleRepository();
        this.vehicleDataMap = new ConcurrentHashMap<>();
        this.playerVehicleDataMap = new ConcurrentHashMap<>();
        this.vehicleIDByUuid = new ConcurrentHashMap<>();
        this.keyId = new NamespacedKey(Main.getInstance(), "id");
        this.keyUuid = new NamespacedKey(Main.getInstance(), "uuid");
        this.keyKm = new NamespacedKey(Main.getInstance(), "km");
        this.keyFuel = new NamespacedKey(Main.getInstance(), "fuel");
        this.keyLock = new NamespacedKey(Main.getInstance(), "lock");
        this.keyType = new NamespacedKey(Main.getInstance(), "type");

        initializeData();
    }

    private void initializeData() {
        try {
            vehicleDataMap.putAll(vehicleRepository.loadVehicles());
            playerVehicleDataMap.putAll(vehicleRepository.loadPlayerVehicles());
        } catch (SQLException e) {
            throw new VehicleServiceException("Failed to initialize vehicle data", e);
        }
    }

    @Override
    public Map<String, VehicleData> getVehicles() {
        return Collections.unmodifiableMap(vehicleDataMap);
    }

    @Override
    public Map<Integer, PlayerVehicleData> getPlayerVehicles() {
        return Collections.unmodifiableMap(playerVehicleDataMap);
    }

    @Override
    public Optional<VehicleData> getVehicleByName(String name) {
        return Optional.ofNullable(vehicleDataMap.get(name));
    }

    @Override
    public Optional<PlayerVehicleData> getPlayerVehicleById(Integer id) {
        return Optional.ofNullable(playerVehicleDataMap.get(id));
    }

    @Override
    public Minecart spawnVehicle(Player player, PlayerVehicleData playerVehicleData) {
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) {
            throw new VehicleServiceException("World '" + WORLD_NAME + "' not found");
        }

        Location location = new Location(
                world,
                playerVehicleData.getX(),
                playerVehicleData.getY() + 1,
                playerVehicleData.getZ(),
                playerVehicleData.getYaw(),
                playerVehicleData.getPitch()
        );

        Minecart minecart = (Minecart) world.spawnEntity(location, EntityType.MINECART);
        PersistentDataContainer container = minecart.getPersistentDataContainer();

        container.set(keyId, PersistentDataType.INTEGER, playerVehicleData.getId());
        container.set(keyUuid, PersistentDataType.STRING, playerVehicleData.getUuid());
        container.set(keyKm, PersistentDataType.INTEGER, playerVehicleData.getKm());
        container.set(keyFuel, PersistentDataType.FLOAT, playerVehicleData.getFuel());
        container.set(keyLock, PersistentDataType.INTEGER, 1);
        container.set(keyType, PersistentDataType.STRING, playerVehicleData.getType());

        playerVehicleData.setLocked(true);

        VehicleData vehicleData = vehicleDataMap.get(playerVehicleData.getType());
        if (vehicleData == null) {
            minecart.remove();
            throw new VehicleServiceException("Vehicle type '" + playerVehicleData.getType() + "' not found");
        }

        minecart.setMaxSpeed(vehicleData.getMaxspeed());
        return minecart;
    }

    @Override
    public void spawnPlayerVehicles(Player player) {
        UUID playerUuid = player.getUniqueId();
        playerVehicleDataMap.values().stream()
                .filter(data -> data.getUuid().equals(playerUuid.toString()))
                .forEach(data -> spawnVehicle(player, data));
    }

    @Override
    public void deleteVehicleById(Integer id) {
        Optional<PlayerVehicleData> vehicleDataOpt = getPlayerVehicleById(id);
        if (!vehicleDataOpt.isPresent()) {
            throw new VehicleServiceException("Vehicle with ID " + id + " not found");
        }

        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) {
            throw new VehicleServiceException("World '" + WORLD_NAME + "' not found");
        }

        for (Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                PersistentDataContainer container = entity.getPersistentDataContainer();
                Integer entityId = container.get(keyId, PersistentDataType.INTEGER);
                if (id.equals(entityId)) {
                    updateVehicleData(entity, id);
                    entity.remove();
                    break;
                }
            }
        }
    }

    @Override
    public void deleteVehicleByUUID(UUID uuid) {
        String uuidString = uuid.toString();
        Integer vehicleId = vehicleIDByUuid.get(uuidString);
        if (vehicleId == null) {
            return;
        }

        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) {
            throw new VehicleServiceException("World '" + WORLD_NAME + "' not found");
        }

        for (Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                PersistentDataContainer container = entity.getPersistentDataContainer();
                String entityUuid = container.get(keyUuid, PersistentDataType.STRING);
                if (uuidString.equals(entityUuid)) {
                    updateVehicleData(entity, vehicleId);
                    entity.remove();
                    break;
                }
            }
        }
    }

    private void updateVehicleData(Entity entity, Integer id) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        PlayerVehicleData vehicleData = playerVehicleDataMap.get(id);
        if (vehicleData == null) {
            return;
        }

        vehicleData.setKm(container.get(keyKm, PersistentDataType.INTEGER));
        vehicleData.setFuel(container.get(keyFuel, PersistentDataType.FLOAT));
        vehicleData.setX((int) entity.getLocation().getX());
        vehicleData.setY((int) entity.getLocation().getY());
        vehicleData.setZ((int) entity.getLocation().getZ());
        vehicleData.setYaw(entity.getLocation().getYaw());
        vehicleData.setPitch(entity.getLocation().getPitch());

        try {
            vehicleData.save();
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("Failed to save vehicle data for ID " + id + ": " + e.getMessage());
        }
    }

    @Override
    public void toggleVehicleState(Integer id, Player player) {
        String PREFIX = "§6Fahrzeug §8┃ §8➜ §7";
        Optional<PlayerVehicleData> vehicleDataOpt = getPlayerVehicleById(id);
        if (!vehicleDataOpt.isPresent()) {
            throw new VehicleServiceException("Vehicle with ID " + id + " not found");
        }

        World world = player.getWorld();
        for (Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                PersistentDataContainer container = entity.getPersistentDataContainer();
                Integer entityId = container.get(keyId, PersistentDataType.INTEGER);
                if (id.equals(entityId)) {
                    PlayerVehicleData vehicleData = vehicleDataOpt.get();
                    Integer lock = container.get(keyLock, PersistentDataType.INTEGER);
                    String vehicleType = container.get(keyType, PersistentDataType.STRING);

                    boolean newLockState = lock == 1;
                    vehicleData.setLocked(newLockState);
                    container.set(keyLock, PersistentDataType.INTEGER, newLockState ? 0 : 1);

                    player.playSound(entity.getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);
                    String message = newLockState
                            ? String.format("Dein %s wurde §aaufgeschlossen§7!", vehicleType)
                            : String.format("Dein %s wurde §czugeschlossen§7!", vehicleType);
                    player.sendMessage(PREFIX + message);
                    break;
                }
            }
        }
    }

    @Override
    public void fillVehicle(Vehicle vehicle, Integer newFuel) {
        PersistentDataContainer container = vehicle.getPersistentDataContainer();
        String type = container.get(keyType, PersistentDataType.STRING);
        if (type == null) {
            throw new VehicleServiceException("Vehicle type not found");
        }

        Optional<VehicleData> vehicleDataOpt = getVehicleByName(type);
        if (!vehicleDataOpt.isPresent()) {
            throw new VehicleServiceException("Vehicle data for type '" + type + "' not found");
        }

        VehicleData vehicleData = vehicleDataOpt.get();
        float currentFuel = container.get(keyFuel, PersistentDataType.FLOAT);
        float newFuelValue = newFuel != null ? currentFuel + newFuel : vehicleData.getMaxFuel();
        container.set(keyFuel, PersistentDataType.FLOAT, newFuelValue);
    }

    @Override
    public Optional<PlayerVehicleData> getNearestVehicle(Location location) {
        PlayerVehicleData nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (PlayerVehicleData data : playerVehicleDataMap.values()) {
            World world = Bukkit.getWorld(WORLD_NAME);
            if (world == null) {
                continue;
            }

            Location dataLocation = new Location(world, data.getX(), data.getY(), data.getZ());
            try {
                double distance = dataLocation.distance(location);
                if (distance < nearestDistance) {
                    nearest = data;
                    nearestDistance = distance;
                }
            } catch (IllegalArgumentException e) {
                // Ignore cross-world distance calculations
            }
        }

        return Optional.ofNullable(nearest);
    }

    @SneakyThrows
    @Override
    public void giveVehicle(Player player, String vehicleName) {
        Optional<VehicleData> vehicleDataOpt = getVehicleByName(vehicleName);
        if (!vehicleDataOpt.isPresent()) {
            throw new VehicleServiceException("Vehicle type '" + vehicleName + "' not found");
        }

        VehicleData vehicleData = vehicleDataOpt.get();
        UUID playerUuid = player.getUniqueId();
        try {
            int vehicleId = vehicleRepository.savePlayerVehicle(playerUuid, vehicleName);
            PlayerVehicleData playerVehicleData = new PlayerVehicleData();
            playerVehicleData.setId(vehicleId);
            playerVehicleData.setUuid(playerUuid.toString());
            playerVehicleData.setType(vehicleName);
            playerVehicleData.setKm(0);
            playerVehicleData.setFuel(vehicleData.getMaxFuel());
            playerVehicleData.setX((int) player.getLocation().getX());
            playerVehicleData.setY((int) player.getLocation().getY());
            playerVehicleData.setZ((int) player.getLocation().getZ());
            playerVehicleData.setWelt(player.getWorld());
            playerVehicleData.setYaw(player.getLocation().getYaw());
            playerVehicleData.setPitch(player.getLocation().getPitch());

            playerVehicleDataMap.put(vehicleId, playerVehicleData);
            vehicleIDByUuid.put(playerUuid.toString(), vehicleId);
            spawnVehicle(player, playerVehicleData);
        } catch (SQLException e) {
            throw new VehicleServiceException("Failed to give vehicle '" + vehicleName + "' to player", e);
        }
    }

    @SneakyThrows
    @Override
    public void removeVehicleFromDatabase(Integer vehicleId) {
        try {
            vehicleRepository.deletePlayerVehicle(vehicleId);
            playerVehicleDataMap.remove(vehicleId);
            vehicleIDByUuid.values().removeIf(id -> id.equals(vehicleId));
        } catch (SQLException e) {
            throw new VehicleServiceException("Failed to remove vehicle with ID " + vehicleId + " from database", e);
        }
    }
}