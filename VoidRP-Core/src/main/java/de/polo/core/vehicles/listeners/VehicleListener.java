package de.polo.core.vehicles.listeners;

import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.game.base.vehicle.PlayerVehicleData;
import de.polo.core.game.base.vehicle.VehicleData;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.storage.GasStationData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Event;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.player.SoundManager;
import de.polo.core.vehicles.services.VehicleService;
import de.polo.core.vehicles.services.exceptions.VehicleServiceException;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.polo.core.Main.scoreboardAPI;

/**
 * Listener for handling vehicle-related events in the game.
 *
 * @author Mayson1337
 * @version 1.1.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Event
public class VehicleListener implements Listener {
    private final Map<Player, Double> playerSpeeds = new HashMap<>();
    private final NamespacedKey keyId = new NamespacedKey(Main.getInstance(), "id");
    private final NamespacedKey keyType = new NamespacedKey(Main.getInstance(), "type");
    private final NamespacedKey keyKm = new NamespacedKey(Main.getInstance(), "km");
    private final NamespacedKey keyFuel = new NamespacedKey(Main.getInstance(), "fuel");
    private final NamespacedKey keyLock = new NamespacedKey(Main.getInstance(), "lock");
    private final NamespacedKey keyUuid = new NamespacedKey(Main.getInstance(), "uuid");

    /**
     * Handles a player entering a vehicle.
     *
     * @param event the vehicle enter event
     */
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getVehicle() instanceof Minecart vehicle) || !(event.getEntered() instanceof Player player)) {
            return;
        }

        PersistentDataContainer container = vehicle.getPersistentDataContainer();
        Integer lockState = container.get(keyLock, PersistentDataType.INTEGER);

        if (lockState == null || lockState == 1) {
            event.setCancelled(true);
            player.sendMessage(Prefix.ERROR + "Das Fahrzeug ist zugeschlossen.");
            return;
        }

        PlayerService playerService = VoidAPI.getService(PlayerService.class);

        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        String job = playerData.getVariable("job");
        if ("pfeifentransport".equals(job)) {
            event.setCancelled(true);
            player.sendMessage(Prefix.ERROR + "Du kannst kein Auto fahren, während du den Pfeifentransport machst!");
            return;
        }

        String type = container.get(keyType, PersistentDataType.STRING);
        setupVehicleScoreboard(player, vehicle, type, playerData);
        playerSpeeds.put(player, 0.0);
    }

    /**
     * Handles a player exiting a vehicle.
     *
     * @param event the vehicle exit event
     */
    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getVehicle() instanceof Minecart vehicle) || !(event.getExited() instanceof Player player)) {
            return;
        }
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        VehicleService vehicleService = VoidAPI.getService(VehicleService.class);

        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        Integer vehicleId = vehicle.getPersistentDataContainer().get(keyId, PersistentDataType.INTEGER);

        if (vehicleId != null) {
            try {
                Optional<PlayerVehicleData> vehicleDataOpt = vehicleService.getPlayerVehicleById(vehicleId);
                if (vehicleDataOpt.isPresent()) {
                    PlayerVehicleData vehicleData = vehicleDataOpt.get();
                    updateVehiclePosition(vehicleData, vehicle.getLocation());
                    vehicleData.save();
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().warning("Failed to save vehicle data on exit for ID " + vehicleId + ": " + e.getMessage());
            }
        }

        scoreboardAPI.removeScoreboard(player, "vehicle");
        playerSpeeds.remove(player);
    }

    /**
     * Handles vehicle movement to control speed and acceleration.
     *
     * @param event the vehicle move event
     */
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart vehicle) || event.getVehicle().getPassengers().isEmpty()) {
            return;
        }
        VehicleService vehicleService = VoidAPI.getService(VehicleService.class);

        Player player = (Player) vehicle.getPassengers().get(0);
        String type = vehicle.getPersistentDataContainer().get(keyType, PersistentDataType.STRING);

        Optional<VehicleData> vehicleDataOpt = vehicleService.getVehicleByName(type);
        if (!vehicleDataOpt.isPresent()) {
            return;
        }

        VehicleData vehicleData = vehicleDataOpt.get();
        double maxSpeed = vehicleData.getMaxspeed();
        double acceleration = vehicleData.getAcceleration();
        double currentSpeed = playerSpeeds.getOrDefault(player, 0.0);

        if (currentSpeed < maxSpeed) {
            currentSpeed += acceleration;
            if (currentSpeed > maxSpeed) {
                currentSpeed = maxSpeed;
            }
        }

        playerSpeeds.put(player, currentSpeed);
        double speedMetersPerSecond = currentSpeed * 1000.0 / 36000.0;
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Vector newVelocity = direction.multiply(speedMetersPerSecond);
        vehicle.setVelocity(newVelocity);
    }

    /**
     * Handles player interaction with gas stations.
     *
     * @param event the player interact event
     */
    @EventHandler
    public void onGasStationInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.LEVER) {
            return;
        }

        RegisteredBlock registeredBlock = Main.blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
        if (registeredBlock == null || !"gas".equalsIgnoreCase(registeredBlock.getInfo())) {
            return;
        }

        int stationId = Integer.parseInt(registeredBlock.getInfoValue());
        if (stationId == 0) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        LocationService locationService = VoidAPI.getService(LocationService.class);

        GasStationData gasStationData = locationService.getGasStations().stream().filter(x -> x.getId() == stationId).findFirst().orElse(null);
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());

        InventoryManager inventoryManager = buildGasStationInventory(player, gasStationData, playerData);
        if (inventoryManager == null) {
            player.sendMessage(Prefix.ERROR + "Keine Fahrzeuge in der Nähe zum Betanken.");
        }
    }

    /**
     * Prevents damage to vehicles.
     *
     * @param event the vehicle damage event
     */
    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            event.setCancelled(true);
        }
    }

    private void setupVehicleScoreboard(Player player, Minecart vehicle, String type, PlayerData playerData) {
        scoreboardAPI.createScoreboard(player, "vehicle", "§6" + type, () -> {
            if (!player.isInsideVehicle()) {
                return;
            }
            PersistentDataContainer container = vehicle.getPersistentDataContainer();
            int km = container.getOrDefault(keyKm, PersistentDataType.INTEGER, 0);
            float fuel = container.getOrDefault(keyFuel, PersistentDataType.FLOAT, 0.0f);
            double speedMetersPerSecond = vehicle.getVelocity().length();
            double kmh = speedMetersPerSecond * 36;

            scoreboardAPI.setScore(player, "vehicle", "§eKMH§8:", (int) kmh);
            scoreboardAPI.setScore(player, "vehicle", "§eKM§8:", km * 2);
            scoreboardAPI.setScore(player, "vehicle", "§eTank§8:", (int) fuel);
        });
        playerData.setScoreboard("vehicle", scoreboardAPI.getScoreboard(player, "vehicle"));
    }

    private void updateVehiclePosition(PlayerVehicleData vehicleData, Location location) {
        vehicleData.setX((int) location.getX());
        vehicleData.setY((int) location.getY());
        vehicleData.setZ((int) location.getZ());
        vehicleData.setYaw(location.getYaw());
        vehicleData.setPitch(location.getPitch());
    }

    private InventoryManager buildGasStationInventory(Player player, GasStationData gasStationData, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 9, Component.text("§8 » §6Tankstelle"), true, false);
        int slot = 0;

        for (Entity entity : player.getWorld().getEntities()) {
            if (!(entity instanceof Minecart) || player.getLocation().distance(entity.getLocation()) > 8) {
                continue;
            }

            PersistentDataContainer container = entity.getPersistentDataContainer();
            String uuid = container.get(keyUuid, PersistentDataType.STRING);
            if (!player.getUniqueId().toString().equals(uuid)) {
                continue;
            }

            String type = container.get(keyType, PersistentDataType.STRING);
            float fuel = container.get(keyFuel, PersistentDataType.FLOAT);
            VehicleService vehicleService = VoidAPI.getService(VehicleService.class);

            Optional<VehicleData> vehicleDataOpt = vehicleService.getVehicleByName(type);
            if (!vehicleDataOpt.isPresent()) {
                continue;
            }

            VehicleData vehicleData = vehicleDataOpt.get();
            int fuelDifference = vehicleData.getMaxFuel() - (int) fuel;
            int finalSlot = slot;

            playerData.setIntVariable("current_fuel", (int) fuel);
            playerData.setIntVariable("plusfuel", 0);

            inventoryManager.setItem(new CustomItem(finalSlot, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + type, Arrays.asList(
                    "§7 ➥ §8[§6Linksklick§8]§7 Tankoptionen",
                    "§7 ➥ §8[§6Rechtsklick§8]§7 Volltanken (§a" + fuelDifference * gasStationData.getLiterprice() + "$§7)"
            ))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (event.isLeftClick()) {
                        playerData.setIntVariable("plusfuel", 0);
                        playerData.setIntVariable("current_fuel", (int) fuel);
                        updateGasInventory(player, (Minecart) entity, gasStationData, vehicleData);
                    } else if (event.isRightClick()) {
                        handleFullRefuel(player, (Minecart) entity, fuelDifference, gasStationData.getLiterprice());
                    }
                }
            });
            slot++;
        }

        return slot > 0 ? inventoryManager : null;
    }

    private void updateGasInventory(Player player, Minecart vehicle, GasStationData gasStationData, VehicleData vehicleData) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        InventoryManager inv = new InventoryManager(player, 27, Component.text("§8 » §6Tankstelle"), true, false);

        inv.setItem(new CustomItem(10, ItemManager.createItem(Material.PURPLE_DYE, 1, 0, "§5-10 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                adjustFuel(playerData, -10);
                updateGasInventory(player, vehicle, gasStationData, vehicleData);
            }
        });

        inv.setItem(new CustomItem(11, ItemManager.createItem(Material.MAGENTA_DYE, 1, 0, "§d-1 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                adjustFuel(playerData, -1);
                updateGasInventory(player, vehicle, gasStationData, vehicleData);
            }
        });

        inv.setItem(new CustomItem(15, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§a+1 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                adjustFuel(playerData, 1);
                updateGasInventory(player, vehicle, gasStationData, vehicleData);
            }
        });

        inv.setItem(new CustomItem(16, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§2+10 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                adjustFuel(playerData, 10);
                updateGasInventory(player, vehicle, gasStationData, vehicleData);
            }
        });

        float currentFuel = playerData.getIntVariable("current_fuel");
        inv.setItem(new CustomItem(13, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + vehicleData.getName(), "§7 ➥ §e" + Math.floor(currentFuel) + " Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });

        inv.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen", "§7 ➥ §7Kosten: " + gasStationData.getLiterprice() * playerData.getIntVariable("plusfuel") + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                handleCustomRefuel(player, vehicle, gasStationData.getLiterprice(), playerData.getIntVariable("plusfuel"));
            }
        });
    }

    private void adjustFuel(PlayerData playerData, int amount) {
        int plusFuel = playerData.getIntVariable("plusfuel");
        if (plusFuel + amount >= 0) {
            playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") + amount);
            playerData.setIntVariable("plusfuel", plusFuel + amount);
        }
    }

    private void handleFullRefuel(Player player, Minecart vehicle, int fuelDifference, int literPrice) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        VehicleService vehicleService = VoidAPI.getService(VehicleService.class);
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        int price = fuelDifference * literPrice;

        if (playerData.getBargeld() < price) {
            player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei (§a" + price + "$§7).");
            return;
        }

        try {
            vehicleService.fillVehicle(vehicle, null);
            playerService.removeMoney(player, price, "Tankrechnung " + vehicle.getPersistentDataContainer().get(keyType, PersistentDataType.STRING));
            player.sendMessage(Prefix.MAIN + "Du hast dein §6" + vehicle.getPersistentDataContainer().get(keyType, PersistentDataType.STRING) + "§7 betankt. §c-" + price + "$");
            player.closeInventory();
        } catch (VehicleServiceException e) {
            player.sendMessage(Prefix.ERROR + "Fehler beim Betanken: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCustomRefuel(Player player, Minecart vehicle, int literPrice, int plusFuel) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        VehicleService vehicleService = VoidAPI.getService(VehicleService.class);
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        int price = literPrice * plusFuel;

        if (playerData.getBargeld() < price) {
            player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei (§a" + price + "$§7).");
            return;
        }

        try {
            vehicleService.fillVehicle(vehicle, plusFuel);
            playerService.removeMoney(player, price, "Tankrechnung " + vehicle.getPersistentDataContainer().get(keyType, PersistentDataType.STRING));
            player.sendMessage(Prefix.MAIN + "Du hast dein §6" + vehicle.getPersistentDataContainer().get(keyType, PersistentDataType.STRING) + "§7 betankt. §c-" + price + "$");
            player.closeInventory();
            SoundManager.successSound(player);
        } catch (VehicleServiceException e) {
            player.sendMessage(Prefix.ERROR + "Fehler beim Betanken: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}