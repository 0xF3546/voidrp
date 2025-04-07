package de.polo.voidroleplay.game.base.extra.drop;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.NavigationManager;
import de.polo.voidroleplay.storage.NaviData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

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
        hologram.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "id"), PersistentDataType.INTEGER, 0);
        hologram.setVisible(false);
        hologram.setCustomNameVisible(true);
        hologram.setGravity(false);
        hologram.setMarker(true);
        setMinutes(8);
        double[] x = {location.getX() - 50, location.getX() + 50, location.getX() + 50, location.getX() - 50};
        double[] z = {location.getZ() - 50, location.getZ() - 50, location.getZ() + 50, location.getZ() + 50};
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

    public void open(Player player) {
        cleanup();
        player.sendMessage("§7   ===§8[§cDrop§8]§7===");
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        int snuff = Utils.random(0, 20);
        if (snuff > 0) {
            playerData.getInventory().addItem(RoleplayItem.SNUFF, snuff);
            player.sendMessage("§8 - " + snuff + "x " + RoleplayItem.SNUFF.getDisplayName());
        }
        int cigar = Utils.random(0, 20);
        if (cigar > 0) {
            playerData.getInventory().addItem(RoleplayItem.CIGAR, cigar);
            player.sendMessage("§8 - " + cigar + "x " + RoleplayItem.CIGAR.getDisplayName());
        }

        boolean kevlar = Utils.random(0, 4) == 1;
        if (kevlar) {
            ItemManager.addCustomItem(player, RoleplayItem.BULLETPROOF, 1);
            player.sendMessage("§8 - " + RoleplayItem.BULLETPROOF.getDisplayName());
        }

        boolean heavy_kevlar = Utils.random(0, 8) == 1;
        if (!kevlar && heavy_kevlar) {
            ItemManager.addCustomItem(player, RoleplayItem.HEAVY_BULLETPROOF, 1);
            player.sendMessage("§8 - " + RoleplayItem.HEAVY_BULLETPROOF.getDisplayName());
        }
    }
}
