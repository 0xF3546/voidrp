package de.polo.voidroleplay.game.base.vehicle;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.*;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.player.ScoreboardAPI;
import de.polo.voidroleplay.utils.player.SoundManager;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static de.polo.voidroleplay.Main.utils;

public class Vehicles implements Listener, CommandExecutor {
    public static final Map<String, VehicleData> vehicleDataMap = new HashMap<>();
    public static final Map<Integer, PlayerVehicleData> playerVehicleDataMap = new HashMap<>();
    public static final HashMap<String, Integer> vehicleIDByUUid = new HashMap<>();

    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final HashMap<Player, Double> playerSpeeds = new HashMap<>();

    public Vehicles(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Main.registerCommand("car", this);
        try {
            loadVehicles();
            loadPlayerVehicles();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void spawnPlayerVehicles(Player player) {
        for (PlayerVehicleData playerVehicleData : playerVehicleDataMap.values()) {
            if (playerVehicleData.getUuid().equals(player.getUniqueId().toString())) {
                spawnVehicle(player, playerVehicleData);
            }
        }
    }

    public static Minecart spawnVehicle(Player player, PlayerVehicleData playerVehicleData) {
        Location location = new Location(Bukkit.getWorld("world"), playerVehicleData.getX(), playerVehicleData.getY() + 1, playerVehicleData.getZ(), playerVehicleData.getYaw(), playerVehicleData.getPitch());
        Minecart minecart = (Minecart) Bukkit.getWorld("world").spawnEntity(location, EntityType.MINECART);
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
        playerVehicleData.setLocked(true);
        NamespacedKey key_type = new NamespacedKey(Main.plugin, "type");
        minecart.getPersistentDataContainer().set(key_type, PersistentDataType.STRING, playerVehicleData.getType());

        VehicleData vehicleData = vehicleDataMap.get(playerVehicleData.getType());
        minecart.setMaxSpeed(vehicleData.getMaxspeed());
        return minecart;
    }

    public static void deleteVehicleById(Integer id) throws SQLException {
        for (Entity entity : Bukkit.getWorld("world").getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                NamespacedKey key_id = new NamespacedKey(Main.plugin, "id");
                if (Objects.equals(entity.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER), id)) {
                    int km = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                    float fuel = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                    PlayerVehicleData playerVehicleData = playerVehicleDataMap.get(id);
                    playerVehicleData.setKm(km);
                    playerVehicleData.setFuel(fuel);
                    playerVehicleData.setX((int) entity.getLocation().getX());
                    playerVehicleData.setY((int) entity.getLocation().getY());
                    playerVehicleData.setZ((int) entity.getLocation().getZ());
                    playerVehicleData.setYaw(entity.getLocation().getYaw());
                    playerVehicleData.setPitch(entity.getLocation().getPitch());
                    playerVehicleData.save();
                    entity.remove();
                }
            }
        }
    }

    public static void deleteVehicleByUUID(String uuid) throws SQLException {
        for (Entity entity : Bukkit.getWorld("world").getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                NamespacedKey key_uuid = new NamespacedKey(Main.plugin, "uuid");
                if (Objects.equals(entity.getPersistentDataContainer().get(key_uuid, PersistentDataType.STRING), uuid)) {
                    int id = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                    int km = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                    float fuel = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                    PlayerVehicleData playerVehicleData = playerVehicleDataMap.get(id);
                    playerVehicleData.setKm(km);
                    playerVehicleData.setFuel(fuel);
                    playerVehicleData.setX((int) entity.getLocation().getX());
                    playerVehicleData.setY((int) entity.getLocation().getY());
                    playerVehicleData.setZ((int) entity.getLocation().getZ());
                    playerVehicleData.setYaw(entity.getLocation().getYaw());
                    playerVehicleData.setPitch(entity.getLocation().getPitch());
                    playerVehicleData.save();
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
                    PlayerVehicleData vehicle = playerVehicleDataMap.get(id);
                    if (lock == 1) {
                        vehicle.setLocked(false);
                        entity.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "lock"), PersistentDataType.INTEGER, 0);
                        player.sendMessage(Prefix.MAIN + "Dein " + entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) + " wurde §aaufgeschlossen§7!");
                    } else {
                        vehicle.setLocked(true);
                        entity.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "lock"), PersistentDataType.INTEGER, 1);
                        player.sendMessage(Prefix.MAIN + "Dein " + entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING) + " wurde §czugeschlossen§7!");
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

    public static PlayerVehicleData getNearestVehicle(Location location) {
        PlayerVehicleData nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (PlayerVehicleData data : playerVehicleDataMap.values()) {
            Location dataLocation = new Location(Bukkit.getWorld("World"), data.getX(), data.getY(), data.getZ());
            double distance = dataLocation.distance(location);

            if (nearest == null || distance < nearestDistance) {
                nearest = data;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private void loadVehicles() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `vehicles`");
        while (result.next()) {
            VehicleData vehicleData = new VehicleData();
            vehicleData.setId(result.getInt(1));
            vehicleData.setName(result.getString(2));
            vehicleData.setAcceleration(result.getFloat(3));
            vehicleData.setMaxspeed(result.getInt(4));
            vehicleData.setPrice(result.getInt(5));
            vehicleData.setMaxFuel(result.getInt(6));
            vehicleData.setTax(result.getInt(7));
            vehicleDataMap.put(result.getString(2), vehicleData);
        }
    }

    private void loadPlayerVehicles() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM `player_vehicles`");
        while (result.next()) {
            PlayerVehicleData playerVehicleData = new PlayerVehicleData();
            playerVehicleData.setId(result.getInt("id"));
            playerVehicleData.setUuid(result.getString("uuid"));
            playerVehicleData.setType(result.getString("type"));
            playerVehicleData.setKm(result.getInt("km"));
            playerVehicleData.setFuel(result.getFloat("fuel"));
            playerVehicleData.setX(result.getInt("x"));
            playerVehicleData.setY(result.getInt("y"));
            playerVehicleData.setZ(result.getInt("z"));
            playerVehicleData.setWelt(Bukkit.getWorld(result.getString(10)));
            playerVehicleData.setYaw(result.getFloat("yaw"));
            playerVehicleData.setPitch(result.getFloat("pitch"));
            playerVehicleData.setFactionId(result.getInt("factionId"));
            playerVehicleDataMap.put(result.getInt(1), playerVehicleData);
            vehicleIDByUUid.put(result.getString(2), result.getInt(1));
        }
    }

    public void giveVehicle(Player player, String vehicle) throws SQLException {
        VehicleData vehicleData = vehicleDataMap.get(vehicle);
        assert vehicleData != null;
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("INSERT INTO `player_vehicles` (`uuid`, `type`) VALUES ('" + player.getUniqueId() + "', '" + vehicleData.getName() + "')");
        ResultSet result = statement.executeQuery("SELECT LAST_INSERT_ID()");
        locationManager.useLocation(player, "vehicleshop_out");
        if (result.next()) {
            PlayerVehicleData playerVehicleData = new PlayerVehicleData();
            playerVehicleData.setId(result.getInt(1));
            playerVehicleData.setUuid(player.getUniqueId().toString());
            playerVehicleData.setType(vehicle);
            playerVehicleData.setKm(0);
            playerVehicleData.setFuel(vehicleData.getMaxFuel());
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

    @SneakyThrows
    public void removeVehicleFromDatabase(int vehicleId) {
        playerVehicleDataMap.remove(vehicleId);
        Main.getInstance().getMySQL().deleteAsync("DELETE FROM player_vehicles WHERE id = ?", vehicleId);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getVehicle().getType().equals(EntityType.MINECART)) {
            NamespacedKey key_lock = new NamespacedKey(Main.plugin, "lock");
            Vehicle vehicle = event.getVehicle();
            Player player = (Player) event.getEntered();
            if (vehicle.getPersistentDataContainer().get(key_lock, PersistentDataType.INTEGER) == 0) {
                PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                ScoreboardAPI scoreboardAPI = Main.getInstance().getScoreboardAPI();
                String type = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
                if (playerData.getVariable("job") != null) {
                    if (playerData.getVariable("job").equals("pfeifentransport")) {
                        event.setCancelled(true);
                        player.sendMessage(Prefix.ERROR + "Du kannst kein Auto fahren, während du den Pfeifentransport machst!");
                        return;
                    }
                }
                scoreboardAPI.createScoreboard(player, "vehicle", "§6" + type, () -> {
                    if (!player.isInsideVehicle()) {
                        return;
                    }
                    int km = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "km"), PersistentDataType.INTEGER);
                    float fuel = vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                    double speedMetersPerSecond = player.getVehicle().getVelocity().length();
                    double kmh = speedMetersPerSecond * 36;
                    scoreboardAPI.setScore(player, "vehicle", "§eKMH§8:", (int) kmh);
                    scoreboardAPI.setScore(player, "vehicle", "§eKM§8:", km * 2);
                    scoreboardAPI.setScore(player, "vehicle", "§eTank§8:", (int) fuel);
                });

                playerData.setScoreboard("vehicle", scoreboardAPI.getScoreboard(player, "vehicle"));
                playerSpeeds.put(player, 0.0);
            } else {
                event.setCancelled(true);
                player.sendMessage(Prefix.ERROR + "Das Fahrzeug ist zugeschlossen.");
            }

        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle().getType().equals(EntityType.MINECART)) {
            ScoreboardAPI scoreboardAPI = Main.getInstance().getScoreboardAPI();
            Vehicle vehicle = event.getVehicle();
            Player player = (Player) event.getExited();
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            //playerData.getScoreboard("vehicle").killScoreboard();
            int id = event.getVehicle().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
            PlayerVehicleData playerVehicleData = Vehicles.playerVehicleDataMap.get(id);
            playerVehicleData.setX((int) event.getVehicle().getLocation().getX());
            playerVehicleData.setY((int) event.getVehicle().getLocation().getY());
            playerVehicleData.setZ((int) event.getVehicle().getLocation().getZ());
            playerVehicleData.save();
            scoreboardAPI.removeScoreboard(player, "vehicle");
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle().getType().equals(EntityType.MINECART)) {
            Vehicle vehicle = event.getVehicle();
            if (vehicle.getPassengers().size() == 0) return;
            if (event.getVehicle().getPassengers().get(0) != null) {
                Player player = (Player) event.getVehicle().getPassengers().get(0);
                VehicleData vehicleData = vehicleDataMap.get(vehicle.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING));
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
                Vector direction = player.getLocation().getDirection().setY(player.getLocation().getDirection().getY()).normalize();
                Vector newVelocity = direction.multiply(speedMetersPerSecond);
                if (direction.getY() < 0) {
                    player.getVehicle().setVelocity(newVelocity);
                }
            }
        }
    }

    @EventHandler
    public void onGasStationInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.LEVER) {
                RegisteredBlock registeredBlock = Main.getInstance().blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                if (registeredBlock == null) return;
                if (!registeredBlock.getInfo().equalsIgnoreCase("gas")) return;
                int station = Integer.parseInt(registeredBlock.getInfoValue());
                if (station != 0) {
                    event.setCancelled(true);
                    GasStationData gasStationData = LocationManager.gasStationDataMap.get(station);
                    Player player = event.getPlayer();
                    PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                    int i = 0;
                    for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
                        if (entity.getType() == EntityType.MINECART) {
                            NamespacedKey key_uuid = new NamespacedKey(Main.plugin, "uuid");
                            if (Objects.equals(entity.getPersistentDataContainer().get(key_uuid, PersistentDataType.STRING), player.getUniqueId().toString()) && player.getLocation().distance(entity.getLocation()) <= 8) {
                                String type = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "type"), PersistentDataType.STRING);
                                float fuel = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "fuel"), PersistentDataType.FLOAT);
                                VehicleData vehicleData = vehicleDataMap.get(type);
                                int dif = vehicleData.getMaxFuel() - (int) fuel;
                                playerData.setIntVariable("current_fuel", (int) fuel);
                                playerData.setIntVariable("plusfuel", 0);
                                InventoryManager inventoryManager = new InventoryManager(player, 9, "§8 » §6Tankstelle", true, false);

                                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + type, Arrays.asList("§7 ➥ §8[§6Linksklick§8]§7 Tankoptionen", "§7 ➥ §8[§6Rechtsklick§8]§7 Volltanken (§a" + dif * gasStationData.getLiterprice() + "$§7)"))) {
                                    @SneakyThrows
                                    @Override
                                    public void onClick(InventoryClickEvent event) {
                                        if (event.isLeftClick()) {
                                            playerData.setIntVariable("plusfuel", 0);
                                            playerData.setIntVariable("current_fuel", (int) Math.floor(fuel));
                                            updateGasInventory(player, entity, gasStationData, vehicleData);
                                        } else if (event.isRightClick()) {
                                            int price = (int) (vehicleData.getMaxFuel() - fuel) * gasStationData.getLiterprice();
                                            if (playerData.getBargeld() >= price) {
                                                player.closeInventory();
                                                Vehicles.fillVehicle((Vehicle) entity, null);
                                                playerManager.removeMoney(player, price, "Tankrechnung " + type);
                                                player.sendMessage(Prefix.MAIN + "Du hast dein §6" + type + "§7 betankt. §c-" + price + "$");
                                            } else {
                                                player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei (§a" + price + "$§7).");
                                            }
                                        }
                                    }
                                });
                                i++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateGasInventory(Player player, Entity entity, GasStationData gasStationData, VehicleData vehicleData) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inv = new InventoryManager(player, 27, "§8 » §6Tankstelle", true, false);
        inv.setItem(new CustomItem(10, ItemManager.createItem(Material.PURPLE_DYE, 1, 0, "§5-10 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                int pFuel = playerData.getIntVariable("plusfuel");
                if (0 <= pFuel - 10) {
                    playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") - 10);
                    playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") - 10);
                    updateGasInventory(player, entity, gasStationData, vehicleData);
                }
            }
        });
        inv.setItem(new CustomItem(11, ItemManager.createItem(Material.MAGENTA_DYE, 1, 0, "§d-1 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                int pFuel = playerData.getIntVariable("plusfuel");
                if (0 <= pFuel - 1) {
                    playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") - 1);
                    playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") - 1);
                    updateGasInventory(player, entity, gasStationData, vehicleData);
                }
            }
        });
        inv.setItem(new CustomItem(15, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§a+1 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                int pFuel = playerData.getIntVariable("plusfuel");
                if (0 <= pFuel + 1) {
                    playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") + 1);
                    playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") + 1);
                    updateGasInventory(player, entity, gasStationData, vehicleData);
                }
            }
        });
        inv.setItem(new CustomItem(16, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§2+10 Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                int pFuel = playerData.getIntVariable("plusfuel");
                if (0 <= pFuel + 10) {
                    playerData.setIntVariable("current_fuel", playerData.getIntVariable("current_fuel") + 10);
                    playerData.setIntVariable("plusfuel", playerData.getIntVariable("plusfuel") + 10);
                    updateGasInventory(player, entity, gasStationData, vehicleData);
                }
            }
        });
        float f = playerData.getIntVariable("current_fuel");
        inv.setItem(new CustomItem(13, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + vehicleData.getName(), "§7 ➥ §e" + Math.floor(f) + " Liter")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inv.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen", "§7 ➥ §7Kosten: " + gasStationData.getLiterprice() * playerData.getIntVariable("plusfuel") + "$")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                int price = gasStationData.getLiterprice() * playerData.getIntVariable("plusfuel");
                if (playerData.getBargeld() >= price) {
                    player.closeInventory();
                    playerManager.removeMoney(player, price, "Tankrechnung " + vehicleData.getName());
                    Vehicles.fillVehicle((Vehicle) entity, playerData.getIntVariable("plusfuel"));
                    player.sendMessage(Prefix.MAIN + "Du hast dein §6" + vehicleData.getName() + "§7 betankt. §c-" + price + "$");
                    SoundManager.successSound(player);
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei (§a" + price + "$§7).");
                }
            }
        });
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) event.getVehicle();
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("start")) {
                Minecart minecart = (Minecart) player.getVehicle();
                if (minecart != null && minecart.isValid()) {
                    minecart.setVelocity(player.getFacing().getDirection().setY(0).multiply(1 + minecart.getVelocity().length() * 2));
                    player.sendMessage(Prefix.MAIN + "Du hast dein Auto gestartet.");
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
            if (args[0].equalsIgnoreCase("find")) {
                InventoryManager inventoryManager = new InventoryManager(player, 9, "§8 » §cFahrzeug suchen", true, false);
                int i = 0;
                for (PlayerVehicleData data : playerVehicleDataMap.values()) {
                    if (data.getUuid().equalsIgnoreCase(player.getUniqueId().toString())) {
                        inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§c" + data.getType())) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                utils.navigationManager.createNaviByCord(player, data.getX(), data.getY(), data.getZ());
                                player.sendMessage("§aDein Fahrzeug wurde markiert.");
                            }
                        });
                        i++;
                    }
                }
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /car [start/stop/lock/find]");
        }
        return false;
    }
}
