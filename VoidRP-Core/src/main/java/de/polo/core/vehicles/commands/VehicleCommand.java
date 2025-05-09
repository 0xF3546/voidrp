package de.polo.core.vehicles.commands;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.game.base.vehicle.PlayerVehicleData;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.NavigationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.vehicles.services.VehicleService;
import de.polo.core.vehicles.services.exceptions.VehicleServiceException;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Command for managing player vehicles, such as starting, locking, and finding them.
 *
 * @author Mayson1337
 * @version 1.1.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "car",
        usage = "/car [start/lock/find]"
)
public class VehicleCommand extends CommandBase {
    private static final String PREFIX = "§6Fahrzeug §8┃ §8➜ §7";

    /**
     * Constructs a new VehicleCommand with the given metadata and services.
     *
     * @param meta the command metadata
     */
    public VehicleCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length == 0) {
            player.sendMessage(PREFIX + "Syntax-Fehler: /car [start/lock/find]");
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "start":
                handleStartCommand(player.getPlayer());
                break;
            case "lock":
                handleLockCommand(player);
                break;
            case "find":
                handleFindCommand(player);
                break;
            default:
                showSyntax(player);
                break;
        }
    }

    /**
     * Handles the 'start' subcommand to accelerate the player's current vehicle.
     *
     * @param player the player executing the command
     */
    private void handleStartCommand(Player player) {
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof Minecart minecart)) {
            player.sendMessage(PREFIX + "Du sitzt in keinem Fahrzeug.");
            return;
        }

        if (!minecart.isValid()) {
            player.sendMessage(PREFIX + "Das Fahrzeug ist nicht verfügbar.");
            return;
        }

        Vector direction = player.getFacing().getDirection().setY(0);
        double speedMultiplier = 1 + minecart.getVelocity().length() * 2;
        minecart.setVelocity(direction.multiply(speedMultiplier));
        player.sendMessage(PREFIX + "Du hast dein Auto gestartet.");
    }

    /**
     * Handles the 'lock' subcommand to display and toggle the lock state of nearby vehicles.
     *
     * @param player the player executing the command
     */
    private void handleLockCommand(VoidPlayer player) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §cSchlüssel"), true, false);
        VehicleService vehicleService = VoidAPI.getService(VehicleService.class);
        int slot = 0;

        for (PlayerVehicleData vehicleData : vehicleService.getPlayerVehicles().values()) {
            if (!vehicleData.getUuid().equals(player.getUuid().toString())) {
                continue;
            }

            Optional<Entity> nearbyVehicle = findNearbyVehicle(player.getPlayer(), vehicleData);
            if (nearbyVehicle.isPresent()) {
                Entity entity = nearbyVehicle.get();
                int finalSlot = slot;
                inventoryManager.setItem(new CustomItem(finalSlot, new ItemBuilder(Material.MINECART)
                        .setName("§c" + vehicleData.getType())
                        .setLore(Arrays.asList(
                                "§7 ➥ §cID: " + vehicleData.getId(),
                                vehicleData.isLocked() ? "§7 ➥ §cZugeschlossen" : "§7 ➥ §aAufgeschlossen"
                        ))
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        try {
                            vehicleService.toggleVehicleState(vehicleData.getId(), player.getPlayer());
                            player.getPlayer().closeInventory();
                        } catch (VehicleServiceException e) {
                            player.sendMessage(PREFIX + "Fehler beim Ändern des Schlosszustands: " + e.getMessage());
                        }
                    }
                });
                slot++;
            }
        }

        if (slot == 0) {
            player.sendMessage(PREFIX + "Keine Fahrzeuge in der Nähe gefunden.");
        }
    }

    /**
     * Handles the 'find' subcommand to display a list of vehicles for navigation.
     *
     * @param player the player executing the command
     */
    private void handleFindCommand(VoidPlayer player) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §cFahrzeug suchen"), true, false);
        VehicleService vehicleService = VoidAPI.getService(VehicleService.class);
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        int slot = 0;

        for (PlayerVehicleData data : vehicleService.getPlayerVehicles().values()) {
            if (!data.getUuid().equalsIgnoreCase(player.getUuid().toString())) {
                continue;
            }

            int finalSlot = slot;
            inventoryManager.setItem(new CustomItem(finalSlot, ItemManager.createItem(Material.MINECART, 1, 0, "§c" + data.getType())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    navigationService.createNaviByCord(player.getPlayer(), data.getX(), data.getY(), data.getZ());
                    player.sendMessage("§aDein Fahrzeug wurde markiert.");
                    player.getPlayer().closeInventory();
                }
            });
            slot++;
        }

        if (slot == 0) {
            player.sendMessage(PREFIX + "Du besitzt keine Fahrzeuge.");
        }
    }

    /**
     * Finds a nearby vehicle entity matching the given vehicle data.
     *
     * @param player      the player to check proximity for
     * @param vehicleData the vehicle data to match
     * @return an Optional containing the nearby vehicle entity, or empty if none found
     */
    private Optional<Entity> findNearbyVehicle(Player player, PlayerVehicleData vehicleData) {
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Minecart && player.getLocation().distance(entity.getLocation()) <= 8) {
                Integer entityId = entity.getPersistentDataContainer().get(
                        new NamespacedKey(Main.getInstance(), "id"), PersistentDataType.INTEGER);
                if (vehicleData.getId() == entityId) {
                    return Optional.of(entity);
                }
            }
        }
        return Optional.empty();
    }
}