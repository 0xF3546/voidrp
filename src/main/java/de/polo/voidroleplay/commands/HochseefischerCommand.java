package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.events.SecondTickEvent;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.NavigationManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.storage.LocationData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static de.polo.voidroleplay.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "hochseefischer", usage = "/hochseefischer")
public class HochseefischerCommand extends CommandBase implements Listener {
    private static final HashMap<Player, Boat> spawnedBoats = new HashMap<>();
    private static final ObjectList<Location> spawnLocations = new ObjectArrayList<>();

    public HochseefischerCommand(@NotNull CommandMeta meta) {
        super(meta);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        for (LocationData locationData : locationManager.getLocations().stream().filter(x -> x.getType() != null && x.getType().equalsIgnoreCase("hochseefischer")).toList()) {
            spawnLocations.add(locationData.getLocation());
        }

    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (!ServerManager.canDoJobs()) {
            player.sendMessage(Component.text(ServerManager.error_cantDoJobs));
            return;
        }
        if (locationManager.getDistanceBetweenCoords(player, "hochseefischer") > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe des Hochseefischers."));
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §bHochseefischer", true, true);
        if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "hochseefischer") && playerData.getVariable("job") == null) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aHochseefischer starten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    startJob(player);
                    player.closeInventory();
                }
            });
        } else {
            if (playerData.getVariable("job") == null) {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHochseefischer starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "hochseefischer")) + "§7.")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHochseefischer starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen.")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            }
        }
        if (playerData.getVariable("job") == null) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        } else {
            if (!playerData.getVariable("job").toString().equalsIgnoreCase("hochseefischer")) {
                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + (int) playerData.getVariable("hochseefischer_kg") * ServerManager.getPayout("hochseefischer") + "$")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        quitJob(player, false);
                    }
                });
            }
        }
    }

    private void startJob(Player player) {
        Boat boat = (Boat) player.getWorld().spawnEntity(locationManager.getLocation("hochseefischer_boat_out"), EntityType.BOAT);
        boat.addPassenger(player);
        spawnedBoats.put(player, boat);
        player.sendMessage(Component.text());
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        List<Location> playerLocations = new ObjectArrayList<>();
        playerData.setVariable("job::hochseefischer::locations", playerLocations);
        playerData.setVariable("job", "hochseefischer");
        playerData.setVariable("hochseefischer_kg", 0);
        Location location = getNearstLocation(player);
        utils.getNavigationManager().createNaviByCord(player, (int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    private void quitJob(Player player, boolean silent) {
        Boat boat = spawnedBoats.get(player);
        Main.getInstance().getCooldownManager().setCooldown(player, "hochseefischer", 1200);
        if (boat != null) boat.remove();
        spawnedBoats.remove(player);
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        // ISSUE VRP-10000: fixed by adding null check
        if (playerData == null) return;
        playerData.setVariable("job", null);
        int amount = playerData.getVariable("hochseefischer_kg");
        Main.getInstance().getPlayerManager().addExp(player, amount * 2);
        playerData.addBankMoney(amount * ServerManager.getPayout("hochseefischer"), "Hochseefischer");
    }

    public static Collection<Location> getLocations() {
        return spawnLocations;
    }

    public static Collection<Player> getPlayers() {
        return spawnedBoats.keySet();
    }

    public static Location getNearstLocation(Player player) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        System.out.println(getLocations().size());
        List<Location> playerLocations = playerData.getVariable("job::hochseefischer::locations");
        Location nearestLocation = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Location location : HochseefischerCommand.getLocations()) {
            double distance = player.getLocation().distance(location);
            if (distance < nearestDistance && !playerLocations.contains(location)) {
                nearestDistance = distance;
                nearestLocation = location;
            }
        }
        return nearestLocation;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        quitJob(event.getPlayer(), true);
    }

    @EventHandler
    public void onSecond(SecondTickEvent event) {
        Random random = new Random();
        for (Player player : spawnedBoats.keySet()) {
            for (Location location : spawnLocations) {
                for (int i = 0; i < 10; i++) {
                    Location randomLocation = getRandomLocationInRadius(location, 5, random);
                    player.spawnParticle(Particle.WATER_SPLASH, randomLocation, 2);
                }
            }
        }
    }

    private Location getRandomLocationInRadius(Location center, double radius, Random random) {
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * radius;

        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;

        return center.clone().add(offsetX, 1, offsetZ);
    }

}
