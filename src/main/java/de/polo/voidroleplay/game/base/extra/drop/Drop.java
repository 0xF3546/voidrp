package de.polo.voidroleplay.game.base.extra.drop;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.NavigationManager;
import de.polo.voidroleplay.storage.NaviData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * drop class to handle drop events
 */
public class Drop {

    private static final Logger LOGGER = Logger.getLogger(Drop.class.getName());
    public Location location;
    public boolean isDropOpen = false;
    public boolean dropEnded = false;
    public Chest chest = null;
    List<ItemStack> items = Arrays.asList(
            ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 4, 0, "§7Gepackte Waffe", "§8 ➥ §cSturmgewehr"),
            ItemManager.createItem(Material.CLAY_BALL, 50, 0, "§7Magazin", "§8 ➥ §cSturmgewehr"),
            ItemManager.createItem(Material.LEATHER_HORSE_ARMOR, 1, 0, "§7Gepackte Waffe", "§8 ➥ §cMarksman"),
            ItemManager.createItem(Material.CLAY_BALL, 10, 0, "§7Magazin", "§8 ➥ §cMarksman"),
            ItemManager.createItem(Material.IRON_HORSE_ARMOR, 2, 0, "§7Gepackte Waffe", "§8 ➥ §eMaschinenpistolen"),
            ItemManager.createItem(Material.CLAY_BALL, 50, 0, "§7Magazin", "§8 ➥ §eMaschinenpistolen"),
            ItemManager.createItem(Material.GOLDEN_HORSE_ARMOR, 2, 0, "§7Gepackte Waffe", "§8 ➥ §cFlinte"),
            ItemManager.createItem(Material.CLAY_BALL, 15, 0, "§7Magazin", "§8 ➥ §cFlinte"),
            ItemManager.createItem(RoleplayItem.HEAVY_BULLETPROOF.getMaterial(), 1, 0, RoleplayItem.HEAVY_BULLETPROOF.getDisplayName()),
            ItemManager.createItem(RoleplayItem.BULLETPROOF.getMaterial(), 3, 0, RoleplayItem.BULLETPROOF.getDisplayName()),
            ItemManager.createItem(RoleplayItem.WAFFENTEIL.getMaterial(), 85, 0, RoleplayItem.WAFFENTEIL.getDisplayName()),
            ItemManager.createItem(RoleplayItem.WAFFENTEIL.getMaterial(), 45, 0, RoleplayItem.WAFFENTEIL.getDisplayName()),
            ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 20, 0, RoleplayItem.SNUFF.getDisplayName()),
            ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 100, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName()),
            ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 28, 0, RoleplayItem.SNUFF.getDisplayName()),
            ItemManager.createItem(RoleplayItem.PIPE_TOBACCO.getMaterial(), 130, 0, RoleplayItem.PIPE_TOBACCO.getDisplayName()),
            ItemManager.createItem(RoleplayItem.PIPE.getMaterial(), 27, 0, RoleplayItem.PIPE.getDisplayName()),
            ItemManager.createItem(RoleplayItem.PIPE.getMaterial(), 20, 0, RoleplayItem.PIPE.getDisplayName())
    );
    private Block lastBlock = null;
    private int minutes = 8;
    private ArmorStand hologram = null;

    public Drop(Location location) {
        lastBlock = location.getBlock();
        this.location = location;
        NaviData naviData = NavigationManager.getNearestNaviPoint(location);
        Location naviLocation = Main.getInstance().locationManager.getLocation(naviData.getLocation());
        if (location.distance(naviLocation) > 100) {
            Bukkit.broadcastMessage("§8[§cDrop§8] §cSchmuggler haben eine Kiste verloren. Informanten haben die Koordinaten X: " + location.getX() + " Y: " + location.getY() + " Z: " + location.getZ() + " übermittelt.");
        } else {
            RegisteredBlock block = Main.getInstance().blockManager.getNearestBlockOfType(location, "house");
            if (block.getLocation().distance(location) < 30) {
                House house = Main.getInstance().houseManager.getHouse(Integer.parseInt(block.getInfoValue()));
                Bukkit.broadcastMessage("§8[§cDrop§8] §cSchmuggler haben eine Kiste in der Nähe von Haus " + house.getNumber() + " verloren.");
            } else {
                Bukkit.broadcastMessage("§8[§cDrop§8] §cSchmuggler haben eine Kiste in der Nähe von " + naviData.getName().replace("&", "§") + " §cverloren.");
            }
        }
        location.getBlock().setType(Material.CHEST);
        Location hologramLocation = location.clone().add(0.5, 2.5, 0.5);
        hologram = (ArmorStand) location.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
        hologram.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, 0);
        hologram.setVisible(false);
        hologram.setCustomNameVisible(true);
        hologram.setGravity(false);
        hologram.setMarker(true);
        setMinutes(8);
        chest = (Chest) location.getBlock().getState();

        Collections.shuffle(items);
        addItemsToChest();
        double[] x = {location.getX() - 50, location.getX() + 50, location.getX() + 50, location.getX() - 50};
        double[] z = {location.getZ() - 50, location.getZ() - 50, location.getZ() + 50, location.getZ() + 50};
        Utils.createWebAreaMarker("", "Schmugglerkiste", "world", x, z);
    }

    public int getMinutes() {
        return this.minutes;
    }

    public void setMinutes(int minutes) {
        if (minutes == 0) {
            if (!isDropOpen) {
                isDropOpen = true;
                hologram.setCustomName("§6Kiste offen");
                Bukkit.broadcastMessage("§8[§cDrop§8] §cDie von Schmugglern fallen gelassene Kiste ist nun offen.");
                this.minutes = 10;
            } else {
                cleanup();
                Bukkit.broadcastMessage("§8[§cDrop§8] §cDie Kiste ist explodiert.");
                Bukkit.getWorld("world").spawnParticle(Particle.EXPLOSION_HUGE, location, 3);
                Bukkit.getWorld("world").playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            }
            return;
        }
        this.minutes = minutes;
        if (hologram != null && !isDropOpen) {
            hologram.setCustomName("§c" + minutes + " Minuten verbleibend");
        }
    }

    public void cleanup() {
        LOGGER.info("Cleaning up drop.");
        Utils.removeAreaMarker("Schmugglerkiste");
        if (lastBlock != null) {
            LOGGER.info("Removing block at location: " + lastBlock.getLocation());
            lastBlock.getLocation().getBlock().setType(Material.AIR);
        }
        if (hologram != null) {
            LOGGER.info("Removing hologram at location: " + hologram.getLocation());
            hologram.remove();
            hologram = null; // Ensure hologram reference is cleared
        }
        dropEnded = true;
        LOGGER.info("drop cleanup complete.");
    }

    private void addItemsToChest() {
        if (chest != null) {
            Inventory inventory = chest.getInventory();
            int numberOfItems = ThreadLocalRandom.current().nextInt(4) + 2;
            for (int i = 0; i < numberOfItems; i++) {
                inventory.addItem(items.get(i));
            }
        }
    }
}
