package de.polo.voidroleplay.game.base.extra.Drop;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.NaviData;
import de.polo.voidroleplay.dataStorage.RegisteredBlock;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.Navigation;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Drop {

    List<ItemStack> items = Arrays.asList(
            ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 4, 0, "§7Gepackte Waffe", "§8 ➥ §cSturmgewehr"),
            ItemManager.createItem(Material.CHEST, 50, 0, "§7Magazin", "§8 ➥ §cSturmgewehr"),
            ItemManager.createItem(Material.LEATHER_HORSE_ARMOR, 1, 0, "§7Gepackte Waffe", "§8 ➥ §cMarksman"),
            ItemManager.createItem(Material.CHEST, 10, 0, "§7Magazin", "§8 ➥ §cMarksman"),
            ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 4, 0, "§7Gepackte Waffe", "§8 ➥ §cFlinte"),
            ItemManager.createItem(Material.CHEST, 15, 0, "§7Magazin", "§8 ➥ §cFlinte"),
            ItemManager.createItem(Material.IRON_CHESTPLATE, 5, 0, "§7Schwere Schutzweste"),
            ItemManager.createItem(RoleplayItem.WAFFENTEIL.getMaterial(), 85, 0, RoleplayItem.WAFFENTEIL.getDisplayName()),
            ItemManager.createItem(RoleplayItem.WAFFENTEIL.getMaterial(), 45, 0, RoleplayItem.WAFFENTEIL.getDisplayName()),
            ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 20, 0, RoleplayItem.COCAINE.getDisplayName()),
            ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 100, 0, RoleplayItem.MARIHUANA.getDisplayName()),
            ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 28, 0, RoleplayItem.COCAINE.getDisplayName()),
            ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 130, 0, RoleplayItem.MARIHUANA.getDisplayName()),
            ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 27, 0, RoleplayItem.JOINT.getDisplayName()),
            ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 20, 0, RoleplayItem.JOINT.getDisplayName())
    );
    private Block lastBlock = null;
    private int minutes = 1;
    private ArmorStand hologram = null;
    public Location location;
    public boolean isDropOpen = false;
    public boolean dropEnded = false;
    public Chest chest = null;

    public Drop(Location location) {
        lastBlock = location.getBlock();
        this.location = location;
        NaviData naviData = Navigation.getNearestNaviPoint(location);
        Location naviLocation = Main.getInstance().locationManager.getLocation(naviData.getLocation());
        if (location.distance(naviLocation) > 100) {
            Bukkit.broadcastMessage("§8[§cDrop§8] §cSchmuggler haben eine Kiste verloren. Informanten haben die Koordinaten X: " + location.getX() + " Y: " + location.getY() + " Z: " + location.getZ() + " übermittelt.");
        } else {
            RegisteredBlock block = Main.getInstance().blockManager.getNearestBlockOfType(location, "house");
            if (block.getLocation().distance(location) < 30) {
                House house = Main.getInstance().housing.getHouse(Integer.parseInt(block.getInfoValue()));
                Bukkit.broadcastMessage("§8[§cDrop§8] §cSchmuggler haben eine Kiste in der nähe von Haus " + house.getNumber() + " verloren.");
            } else {
                Bukkit.broadcastMessage("§8[§cDrop§8] §cSchmuggler haben eine Kiste in der nähe von " + naviData.getName().replace("&", "§") + " §cverloren.");
            }
        }
        location.getBlock().setType(Material.CHEST);
        Location hologramLocation = location.clone().add(0.5, 2.5, 0.5);
        hologram = (ArmorStand) location.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setCustomNameVisible(true);
        hologram.setGravity(false);
        hologram.setMarker(true);
        setMinutes(1);
        chest = (Chest) location.getBlock().getState();

        Collections.shuffle(items);
        addItemsToChest();
    }

    public void setMinutes(int minutes) {
        if (minutes == 0) {
            if (!isDropOpen) {
                isDropOpen = true;
                hologram.setCustomName("§6Kiste offen");
                Bukkit.broadcastMessage("§8[§cDrop§8] §cDie von Schmugglern fallen gelassene Kiste ist nun offen.");
                this.minutes = 2;
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

    public int getMinutes() {
        return this.minutes;
    }

    public void cleanup() {
        if (lastBlock != null) {
            lastBlock.getLocation().getBlock().setType(Material.AIR);
        }
        if (hologram != null) {
            hologram.remove();
        }
        dropEnded = true;
    }

    private void addItemsToChest() {
        if (chest != null) {
            Inventory inventory = chest.getInventory();
            Random random = new Random();
            int numberOfItems = random.nextInt(4) + 2;
            for (int i = 0; i < numberOfItems; i++) {
                inventory.addItem(items.get(i));
            }
        }
    }
}
