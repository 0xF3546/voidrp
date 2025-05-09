package de.polo.core.game.base.extra.drop;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.game.base.housing.House;
import de.polo.core.location.services.LocationService;
import de.polo.core.location.services.NavigationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.NaviData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.logging.Logger;

public class Drop {

    private static final Logger LOGGER = Logger.getLogger(Drop.class.getName());

    // Configurable constants
    private static final int INITIAL_MINUTES = 8;
    private static final int OPEN_DURATION = 10;
    private static final double MAX_NAVI_DISTANCE = 100.0;
    private static final double HOUSE_RADIUS = 30.0;
    private static final double BROADCAST_RADIUS = 50.0;

    private static final String LOG_PREFIX = "[Drop] ";

    @Getter
    private final Location location;

    public boolean isDropOpen = false;
    public boolean dropEnded = false;
    public Chest chest = null;

    private Block lastBlock = null;
    @Getter
    private int minutes = INITIAL_MINUTES;
    private ArmorStand hologram = null;

    private final ObjectList<Pillager> bandits = new ObjectArrayList<>();
    private BukkitRunnable banditTask;
    private static final int BANDIT_SPAWN_INTERVAL_SECONDS = 120;
    private static final int BANDIT_MAX_DISTANCE = 100;


    public Drop(Location location) {
        this.location = location;
        this.lastBlock = location.getBlock();

        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        LocationService locationService = VoidAPI.getService(LocationService.class);

        NaviData naviData = navigationService.getNearestNaviPoint(location);
        Location naviLocation = locationService.getLocation(naviData.getLocation());

        broadcastLocationMessage(naviData, naviLocation);

        location.getBlock().setType(Material.CHEST);

        Location holoLocation = location.clone().add(0.5, 2.5, 0.5);
        this.hologram = createHologram(holoLocation);

        setMinutes(INITIAL_MINUTES);
        startBanditTask();
    }

    private void broadcast(String message) {
        Bukkit.broadcastMessage("§cDrop §8┃ §c" + message);
    }

    private void broadcastLocationMessage(NaviData naviData, Location naviLocation) {
        if (location.distance(naviLocation) > MAX_NAVI_DISTANCE) {
            broadcast("Schmuggler haben eine Kiste verloren. Informanten haben die Koordinaten X: "
                    + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ() + " übermittelt.");
        } else {
            RegisteredBlock block = Main.blockManager.getNearestBlockOfType(location, "house");
            if (block != null && block.getLocation().distance(location) < HOUSE_RADIUS) {
                House house = Main.houseManager.getHouse(Integer.parseInt(block.getInfoValue()));
                broadcast("Schmuggler haben eine Kiste in der Nähe von Haus " + house.getNumber() + " verloren.");
            } else {
                broadcast("Schmuggler haben eine Kiste in der Nähe von " + naviData.getName().replace("&", "§") + " verloren.");
            }
        }
    }

    private ArmorStand createHologram(Location loc) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setCustomNameVisible(true);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "id"), PersistentDataType.INTEGER, 0);
        return stand;
    }

    public void setMinutes(int minutes) {
        if (minutes == 0) {
            if (!isDropOpen) {
                openDrop();
            } else {
                explodeDrop();
            }
        } else {
            this.minutes = minutes;
            updateHologramTimer();
        }
    }

    private void openDrop() {
        isDropOpen = true;
        if (hologram != null) hologram.setCustomName("§6Kiste offen");
        broadcast("Die von Schmugglern fallen gelassene Kiste ist nun offen.");
        this.minutes = OPEN_DURATION;
    }

    private void explodeDrop() {
        cleanup();
        broadcast("Die Kiste ist explodiert.");
        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.spawnParticle(Particle.EXPLOSION_HUGE, location, 3);
            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }
    }

    private void updateHologramTimer() {
        if (hologram != null && !isDropOpen) {
            hologram.setCustomName("§c" + minutes + " Minuten verbleibend");
        }
    }

    public void cleanup() {
        LOGGER.info(LOG_PREFIX + "Cleaning up drop.");

        if (banditTask != null) {
            banditTask.cancel();
        }

        for (Pillager bandit : bandits) {
            if (!bandit.isDead()) {
                bandit.remove();
            }
        }
        bandits.clear();

        if (lastBlock != null) {
            LOGGER.info(LOG_PREFIX + "Removing block at location: " + lastBlock.getLocation());
            lastBlock.setType(Material.AIR);
        }

        if (hologram != null) {
            LOGGER.info(LOG_PREFIX + "Removing hologram at location: " + hologram.getLocation());
            hologram.remove();
            hologram = null;
        }

        dropEnded = true;
        LOGGER.info(LOG_PREFIX + "Drop cleanup complete.");
    }


    private void startBanditTask() {
        banditTask = new BukkitRunnable() {
            int tickCounter = 0;

            @Override
            public void run() {
                if (dropEnded) {
                    this.cancel();
                    return;
                }

                tickCounter++;

                // Alle 2 Minuten (120 Sekunden * 20 Ticks)
                if (tickCounter % (BANDIT_SPAWN_INTERVAL_SECONDS * 20) == 0) {
                    spawnBandits();
                }

                // Banditen zurückholen, wenn sie zu weit laufen
                for (Pillager bandit : new ObjectArrayList<>(bandits)) {
                    if (bandit.isDead()) {
                        bandits.remove(bandit);
                        continue;
                    }

                    double distance = bandit.getLocation().distance(location);
                    if (distance > BANDIT_MAX_DISTANCE) {
                        bandit.teleport(location.clone().add(
                                Utils.random(-5, 5),
                                0,
                                Utils.random(-5, 5)
                        ));
                    }
                }
            }
        };
        banditTask.runTaskTimer(Main.getInstance(), 20L, 20L); // jede Sekunde
    }

    private void spawnBandits() {
        World world = location.getWorld();
        if (world == null) return;

        Random random = new Random();
        int amount = 2 + random.nextInt(3);

        for (int i = 0; i < amount; i++) {
            double x = location.getX() + random.nextInt(10) - 5;
            double z = location.getZ() + random.nextInt(10) - 5;
            Location spawnLoc = new Location(world, x, location.getY(), z);
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc));

            Pillager bandit = (Pillager) world.spawnEntity(spawnLoc, EntityType.PILLAGER);
            bandit.setCustomName("§cBandit");
            bandit.setCustomNameVisible(true);
            bandit.setPersistent(true);
            bandit.setCanPickupItems(false);
            bandits.add(bandit);
        }

        broadcast("Banditen sind aufgetaucht, um die Kiste zu beschützen!");
    }


    public void open(Player player) {
        cleanup();
        player.sendMessage("§7   ===§8[§cDrop§8]§7===");

        PlayerData data = Main.playerManager.getPlayerData(player);
        rewardItem(data, player, RoleplayItem.SNUFF, 20);
        rewardItem(data, player, RoleplayItem.CIGAR, 20);

        boolean hasKevlar = Utils.random(0, 4) == 1;
        if (hasKevlar) {
            ItemManager.addCustomItem(player, RoleplayItem.BULLETPROOF, 1);
            player.sendMessage("§8 - " + RoleplayItem.BULLETPROOF.getDisplayName());
        } else if (Utils.random(0, 8) == 1) {
            ItemManager.addCustomItem(player, RoleplayItem.HEAVY_BULLETPROOF, 1);
            player.sendMessage("§8 - " + RoleplayItem.HEAVY_BULLETPROOF.getDisplayName());
        }
    }

    private void rewardItem(PlayerData data, Player player, RoleplayItem item, int maxAmount) {
        int amount = Utils.random(0, maxAmount);
        if (amount > 0) {
            data.getInventory().addItem(item, amount);
            player.sendMessage("§8 - " + amount + "x " + item.getDisplayName());
        }
    }
}
