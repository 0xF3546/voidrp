package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.GasStationData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.DataStorage.PlayerVehicleData;
import de.polo.void_roleplay.DataStorage.VehicleData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Vehicles implements Listener, CommandExecutor {
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
        minecart.getPersistentDataContainer().set(key_uuid, PersistentDataType.STRING, playerVehicleData.getUuid());
        NamespacedKey key_km = new NamespacedKey(Main.plugin, "km");
        minecart.getPersistentDataContainer().set(key_km, PersistentDataType.INTEGER, playerVehicleData.getKm());
        NamespacedKey key_fuel = new NamespacedKey(Main.plugin, "fuel");
        minecart.getPersistentDataContainer().set(key_fuel, PersistentDataType.FLOAT, playerVehicleData.getFuel());
        NamespacedKey key_lock = new NamespacedKey(Main.plugin, "lock");
        minecart.getPersistentDataContainer().set(key_lock, PersistentDataType.INTEGER, 1);
        NamespacedKey key_type = new NamespacedKey(Main.plugin, "type");
        minecart.getPersistentDataContainer().set(key_type, PersistentDataType.STRING, playerVehicleData.getType());

        VehicleData vehicleData = vehicleDataMap.get(playerVehicleData.getType());
        minecart.setMaxSpeed(vehicleData.getMaxspeed());
        System.out.println("Vehicle mit ID " + playerVehicleData.getId() + " gespawned.");
        System.out.println(minecart.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER));
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
                    statement.executeUpdate("UPDATE `player_vehicles` SET `km` = " + km + ", `fuel` = " + fuel + ", `x` = " + entity.getLocation().getX() + ", `y` = " + entity.getLocation().getY() + ", `z` = " + entity.getLocation().getZ() + ", `welt` = '" + entity.getWorld().getName() + "', `yaw` = " + entity.getLocation().getYaw() + ", `pitch` = " + entity.getLocation().getPitch() + " WHERE `id` = " + id);
                    entity.remove();
                }
            }
        }
    }

    public static void toggleVehicleState(Integer id, Player player) {
        for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                NamespacedKey key_id = new NamespacedKey(Main.plugin, "id");
                if (Objects.equals(entity.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER), id)) {
                    player.playSound(entity.getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);
                    int lock = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "lock"), PersistentDataType.INTEGER);
                    if (lock == 1) {
                        entity.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "lock"), PersistentDataType.INTEGER, 0);
                        player.sendMessage(Main.prefix + "Dein " + entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) + " wurde §aaufgeschlossen§7!");
                    } else {
                        entity.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "lock"), PersistentDataType.INTEGER, 1);
                        player.sendMessage(Main.prefix + "Dein " + entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) + " wurde §czugeschlossen§7!");
                    }
                }
            }
        }
    }

    public static void fillVehicle(Vehicle vehicle, Integer newFuel) {
        String type = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
        int id = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
        float fuel = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
        VehicleData vehicleData = vehicleDataMap.get(type);
        if (newFuel != null) {
            vehicle.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT, fuel + newFuel);
        } else {
            vehicle.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT, (float) vehicleData.getMaxFuel());
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getVehicle().getType().equals(EntityType.MINECART)) {
            NamespacedKey key_lock = new NamespacedKey(Main.plugin, "lock");
            Vehicle vehicle = event.getVehicle();
            Player player = (Player) event.getEntered();
            if (vehicle.getPersistentDataContainer().get(key_lock, PersistentDataType.INTEGER) == 0) {
                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                playerData.getScoreboard().createCarScoreboard(event.getVehicle());
            } else {
                event.setCancelled(true);
                player.sendMessage(Main.error + "Das Fahrzeug ist zugeschlossen.");
            }

        }
    }
    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle().getType().equals(EntityType.MINECART)) {
            Vehicle vehicle = event.getVehicle();
            Player player = (Player) event.getExited();
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            playerData.getScoreboard().killScoreboard();
        }
    }
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle().getType().equals(EntityType.MINECART)) {
            Vehicle vehicle = event.getVehicle();
            if (event.getVehicle().getPassengers().get(0) != null) {
                Player player = (Player) event.getVehicle().getPassengers().get(0);
                VehicleData vehicleData = vehicleDataMap.get(vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING));
                double newAcc = vehicle.getVelocity().getZ() + vehicleData.getAcceleration();
                Vector speed = new Vector();
                player.getVehicle().setVelocity(player.getLocation().getDirection().setY(0).normalize().multiply(vehicle.getVelocity().length() * vehicleData.getAcceleration()));
                System.out.println(vehicle.getVelocity().length() * vehicleData.getAcceleration());
            }
        }
    }
    @EventHandler
    public void onGasStationInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock().getType() == Material.LEVER) {
            Integer station = LocationManager.isPlayerGasStation(event.getPlayer());
            if (station != 0) {
                event.setCancelled(true);
                GasStationData gasStationData = LocationManager.gasStationDataMap.get(station);
                Player player = event.getPlayer();
                PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                Inventory inv = Bukkit.createInventory(player, 9, "§8 » §6Tankstelle");
                int i = 0;
                for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
                    if (entity.getType() == EntityType.MINECART) {
                        NamespacedKey key_uuid = new NamespacedKey(Main.plugin, "uuid");
                        if (Objects.equals(entity.getPersistentDataContainer().get(key_uuid, PersistentDataType.STRING), player.getUniqueId().toString()) && player.getLocation().distance(entity.getLocation()) <= 8) {
                            String type = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
                            int id = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                            int km = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                            float fuel = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                            int lock = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "lock"), PersistentDataType.INTEGER);
                            VehicleData vehicleData = vehicleDataMap.get(type);
                            inv.setItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + type, "Lädt..."));
                            ItemMeta meta = inv.getItem(i).getItemMeta();
                            int dif = vehicleData.getMaxFuel() - (int) fuel;
                            meta.setLore(Arrays.asList("§7 ➥ §8[§6Linksklick§8]§7 Tankoptionen", "§7 ➥ §8[§6Rechtsklick§8]§7 Volltanken (§a" + dif * gasStationData.getLiterprice() + "$§7)"));
                            meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, id);
                            meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING, type);
                            inv.getItem(i).setItemMeta(meta);
                            i++;
                        }
                    }
                }
                playerData.setVariable("current_inventory", "gasstation");
                playerData.setVariable("current_app", null);
                player.openInventory(inv);
            }
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("start")) {
                Minecart minecart = (Minecart) player.getVehicle();
                if (minecart != null & minecart.isValid()) {
                    minecart.setVelocity(player.getFacing().getDirection().setY(0).multiply(1 + minecart.getVelocity().length() * 2));
                    player.sendMessage(Main.prefix + "Du hast dein Auto gestartet.");
                }
            }
            if (args[0].equalsIgnoreCase("lock")) {
                Inventory inv = Bukkit.createInventory(player, 9, "§8 » §cSchlüssel");
                int i = 0;
                for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
                    if (entity.getType() == EntityType.MINECART) {
                        NamespacedKey key_uuid = new NamespacedKey(Main.plugin, "uuid");
                        if (Objects.equals(entity.getPersistentDataContainer().get(key_uuid, PersistentDataType.STRING), player.getUniqueId().toString()) && player.getLocation().distance(entity.getLocation()) <= 8) {
                            String type = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
                            int id = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                            int km = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                            float fuel = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                            int lock = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "lock"), PersistentDataType.INTEGER);
                            if (lock == 0) {
                                inv.setItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + type, "§7 ➥ §cZuschließen"));
                            } else {
                                inv.setItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + type, "§7 ➥ §aAufschließen"));
                            }
                            ItemMeta meta = inv.getItem(i).getItemMeta();
                            meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, id);
                            inv.getItem(i).setItemMeta(meta);
                            i++;
                        }
                    }
                }
                playerData.setVariable("current_inventory", "carlock");
                player.openInventory(inv);
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /car [start/stop/lock]");
        }
        return false;
    }
}
