package de.polo.core.location.services.impl;

import de.polo.api.player.VoidPlayer;
import de.polo.core.location.services.LocationService;
import de.polo.core.storage.GasStationData;
import de.polo.core.storage.LocationData;
import de.polo.core.storage.NaviData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.polo.core.Main.database;

/**
 * Implementation of the LocationService, handling all location-related operations including
 * locations, shops, gas stations, and navigation data.
 *
 * @author Mayson1337
 * @version 1.2.0
 * @since 1.0.0
 */
@Service
public class CoreLocationService implements LocationService {
    private final List<LocationData> locations = new ObjectArrayList<>();
    private final List<GasStationData> gasStations = new ObjectArrayList<>();
    private final List<NaviData> naviData = new ObjectArrayList<>();
    private final List<Object[]> shops = new ObjectArrayList<>();

    public CoreLocationService() {
        loadDataAsync().exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * Loads all location-related data asynchronously from the database.
     */
    private CompletableFuture<Void> loadDataAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                loadLocations();
                loadShops();
                loadGasStations();
                loadNaviData();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load location data", e);
            }
        });
    }

    private void loadLocations() throws SQLException {
        try (var statement = database.getConnection().createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM locations")) {
            locations.clear();
            while (resultSet.next()) {
                LocationData data = new LocationData();
                data.setId(resultSet.getInt("id"));
                data.setName(resultSet.getString("name").toLowerCase());
                data.setX(resultSet.getInt("x"));
                data.setY(resultSet.getInt("y"));
                data.setZ(resultSet.getInt("z"));
                data.setWelt(resultSet.getString("world"));
                data.setYaw(resultSet.getFloat("yaw"));
                data.setPitch(resultSet.getFloat("pitch"));
                data.setType(resultSet.getString("type"));
                data.setInfo(resultSet.getString("info"));
                locations.add(data);
            }
        }
    }

    private void loadShops() throws SQLException {
        try (var statement = database.getConnection().createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM shops")) {
            shops.clear();
            while (resultSet.next()) {
                shops.add(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("x"),
                        resultSet.getInt("y"),
                        resultSet.getInt("z"),
                        resultSet.getString("world"),
                        resultSet.getFloat("yaw"),
                        resultSet.getFloat("pitch")
                });
            }
        }
    }

    private void loadGasStations() throws SQLException {
        try (var statement = database.getConnection().createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM gasstations")) {
            gasStations.clear();
            while (resultSet.next()) {
                GasStationData data = new GasStationData();
                data.setId(resultSet.getInt("id"));
                data.setName(resultSet.getString("name"));
                data.setX(resultSet.getInt("x"));
                data.setY(resultSet.getInt("y"));
                data.setZ(resultSet.getInt("z"));
                data.setWelt(Bukkit.getWorld(resultSet.getString("world")));
                data.setYaw(resultSet.getFloat("yaw"));
                data.setPitch(resultSet.getFloat("pitch"));
                data.setPrice(resultSet.getInt("price"));
                data.setLiterprice(resultSet.getInt("literprice"));
                data.setLiter(resultSet.getInt("liter"));
                data.setCompany(resultSet.getInt("company"));
                data.setBank(resultSet.getInt("bank"));
                gasStations.add(data);
            }
        }
    }

    private void loadNaviData() throws SQLException {
        try (var statement = database.getConnection().createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM navi")) {
            naviData.clear();
            while (resultSet.next()) {
                NaviData data = new NaviData();
                data.setId(resultSet.getInt("id"));
                data.setisGroup(resultSet.getBoolean("isGroup"));
                data.setGroup(resultSet.getString("group"));
                data.setName(resultSet.getString("name"));
                if (resultSet.getString("location") != null) {
                    data.setLocation(resultSet.getString("location"));
                }
                data.setItem(Material.valueOf(resultSet.getString("item")));
                naviData.add(data);
            }
        }
    }

    @Override
    public void setLocation(String name, Player player) {
        Location loc = player.getLocation();
        CompletableFuture.runAsync(() -> {
            try (var statement = database.getConnection().createStatement()) {
                String table;
                String query;
                String finalCleanName; // Neue Variable für den bereinigten Namen

                if (name.contains("isShop")) {
                    table = "shops";
                    player.sendMessage(Prefix.GAMEDESIGN + "Shop registered.");
                    finalCleanName = name.replace(" ", "").replace("isShop", "");
                } else if (name.contains("isGas")) {
                    table = "gasstations";
                    player.sendMessage(Prefix.GAMEDESIGN + "Gas station registered.");
                    finalCleanName = name.replace(" ", "").replace("isGas", "");
                } else if (name.contains("isGarage")) {
                    table = "garage";
                    player.sendMessage(Prefix.GAMEDESIGN + "Garage registered.");
                    finalCleanName = name.replace(" ", "").replace("isGarage", "");
                } else if (name.contains("isFFA")) {
                    table = "ffa_spawnpoints";
                    player.sendMessage(Prefix.GAMEDESIGN + "FFA-Spawn registered.");
                    finalCleanName = name.replace(" ", "").replace("isFFA", "");
                    query = String.format(
                            "INSERT INTO %s (lobby_type, x, y, z, world, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            table);
                    database.updateAsync(query,
                            finalCleanName,
                            loc.getBlockX(),
                            loc.getBlockY(),
                            loc.getBlockZ(),
                            loc.getWorld().getName(),
                            loc.getYaw(),
                            loc.getPitch());
                    return;
                } else if (name.contains("isDealer")) {
                    table = "dealer";
                    player.sendMessage(Prefix.GAMEDESIGN + "Dealer registered.");
                    finalCleanName = name.replace(" ", "").replace("isDealer", "");
                    query = String.format(
                            "INSERT INTO %s (x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?)",
                            table);
                    database.updateAsync(query,
                            loc.getBlockX(),
                            loc.getBlockY(),
                            loc.getBlockZ(),
                            loc.getYaw(),
                            loc.getPitch());
                    return;
                } else {
                    table = "locations";
                    finalCleanName = name.replace(" ", "").toLowerCase();
                    query = String.format(
                            "INSERT INTO %s (name, x, y, z, world, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            table);
                    LocationData data = new LocationData();
                    data.setId(-1);
                    data.setName(finalCleanName);
                    data.setWelt(loc.getWorld().getName());
                    data.setX(loc.getBlockX());
                    data.setY(loc.getBlockY());
                    data.setZ(loc.getBlockZ());
                    data.setYaw(loc.getYaw());
                    data.setPitch(loc.getPitch());
                    locations.add(data);
                }

                query = String.format(
                        "INSERT INTO %s (name, x, y, z, world, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        table);
                database.updateAsync(query,
                        finalCleanName,
                        loc.getBlockX(),
                        loc.getBlockY(),
                        loc.getBlockZ(),
                        loc.getWorld().getName(),
                        loc.getYaw(),
                        loc.getPitch());
            } catch (SQLException e) {
                player.sendMessage(Prefix.ERROR + "Failed to register location.");
                throw new RuntimeException("Failed to set location", e);
            }
        });
    }

    @Override
    public int createLocation(String name, Location location) {
        return database.insertAndGetKeyAsync(
                "INSERT INTO locations (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)",
                name.toLowerCase(),
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getYaw(),
                location.getPitch()
        ).thenApply(key -> {
            int id = key.orElse(-1);
            if (id != -1) {
                LocationData data = new LocationData();
                data.setId(id);
                data.setName(name.toLowerCase());
                data.setX(location.getBlockX());
                data.setY(location.getBlockY());
                data.setZ(location.getBlockZ());
                data.setWelt(location.getWorld().getName());
                data.setYaw(location.getYaw());
                data.setPitch(location.getPitch());
                locations.add(data);
            }
            return id;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return -1;
        }).join();
    }

    @Override
    public void useLocation(Player player, String name) {
        LocationData data = locations.stream()
                .filter(loc -> loc.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        if (data == null) {
            player.sendMessage(Prefix.ERROR + "This location does not exist.");
            return;
        }
        World world = Bukkit.getWorld(data.getWelt());
        if (world == null) {
            player.sendMessage(Prefix.ERROR + "World not found.");
            return;
        }
        player.teleport(data.getLocation());
    }

    @Override
    public Location getLocation(String name) {
        return locations.stream()
                .filter(loc -> loc.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(data -> {
                    World world = Bukkit.getWorld(data.getWelt());
                    return world != null ? data.getLocation() : null;
                })
                .orElse(null);
    }

    @Override
    public Location getLocation(int databaseId) {
        return locations.stream()
                .filter(data -> data.getId() == databaseId)
                .findFirst()
                .map(data -> {
                    World world = Bukkit.getWorld(data.getWelt());
                    return world != null ? data.getLocation() : null;
                })
                .orElse(null);
    }

    @Override
    public double getDistanceBetweenCoords(Player player, String name) {
        LocationData data = locations.stream()
                .filter(loc -> loc.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        if (data == null) {
            return Double.MAX_VALUE;
        }
        World world = Bukkit.getWorld(data.getWelt());
        if (world == null) {
            return Double.MAX_VALUE;
        }
        return player.getLocation().distance(data.getLocation());
    }

    @Override
    public double getDistanceBetweenCoords(VoidPlayer voidPlayer, String name) {
        return getDistanceBetweenCoords(voidPlayer.getPlayer(), name);
    }

    @Override
    public int isNearShop(Player player) {
        return shops.stream()
                .filter(shop -> {
                    World world = Bukkit.getWorld((String) shop[5]);
                    if (world == null) return false;
                    Location shopLoc = new Location(
                            world,
                            ((Number) shop[2]).doubleValue(),
                            ((Number) shop[3]).doubleValue(),
                            ((Number) shop[4]).doubleValue(),
                            ((Number) shop[6]).floatValue(),
                            ((Number) shop[7]).floatValue()
                    );
                    return player.getLocation().distance(shopLoc) < 4;
                })
                .map(shop -> (int) shop[0])
                .findFirst()
                .orElse(0);
    }

    @Override
    public Integer isPlayerNearOwnHouse(Player player) {
        Location center = player.getLocation();
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = centerX - 3; x <= centerX + 3; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - 3; z <= centerZ + 3; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType().toString().contains("SIGN")) {
                        Sign sign = (Sign) block.getState();
                        if (sign.getLine(2).contains(player.getName())) {
                            return 1; // Platzhalter, da blockManager nicht verfügbar ist
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public Integer isPlayerGasStation(Player player) {
        return gasStations.stream()
                .filter(data -> {
                    World world = data.getWelt();
                    if (world == null) return false;
                    return player.getLocation().distance(new Location(
                            world,
                            data.getX(),
                            data.getY(),
                            data.getZ(),
                            data.getYaw(),
                            data.getPitch()
                    )) < 25;
                })
                .map(GasStationData::getId)
                .findFirst()
                .orElse(0);
    }

    @Override
    public GasStationData getGasStationInRadius(Player player) {
        return gasStations.stream()
                .filter(data -> {
                    World world = data.getWelt();
                    if (world == null) return false;
                    return player.getLocation().distance(new Location(
                            world,
                            data.getX(),
                            data.getY(),
                            data.getZ(),
                            data.getYaw(),
                            data.getPitch()
                    )) < 25;
                })
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getNearestLocation(Player player) {
        return naviData.stream()
                .filter(data -> !data.isGroup() && data.getLocation() != null)
                .min(Comparator.comparingDouble(data -> getDistanceBetweenCoords(player, data.getLocation())))
                .map(NaviData::getLocation)
                .orElse(null);
    }

    @Override
    public Integer getNearestLocationId(Player player) {
        return naviData.stream()
                .filter(data -> !data.isGroup() && data.getLocation() != null)
                .min(Comparator.comparingDouble(data -> getDistanceBetweenCoords(player, data.getLocation())))
                .map(NaviData::getId)
                .orElse(null);
    }

    @Override
    public String isNearFarmingSpot(Player player, int radius) {
        return locations.stream()
                .filter(data -> "farming".equalsIgnoreCase(data.getType()))
                .filter(data -> {
                    World world = Bukkit.getWorld(data.getWelt());
                    if (world == null) return false;
                    return player.getLocation().distance(new Location(
                            world,
                            data.getX(),
                            data.getY(),
                            data.getZ()
                    )) < radius;
                })
                .map(LocationData::getName)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<LocationData> getLocations() {
        return Collections.unmodifiableList(locations);
    }

    @Override
    public boolean isLocationEqual(Location first, Location second) {
        if (first == null || second == null) return false;
        return first.getBlockX() == second.getBlockX() &&
                first.getBlockY() == second.getBlockY() &&
                first.getBlockZ() == second.getBlockZ();
    }

    @Override
    public List<GasStationData> getGasStations() {
        return gasStations;
    }

    @Override
    public List<NaviData> getNavis() {
        return naviData;
    }

    @Override
    public void removeLocation(String location) {
        LocationData locationData = getLocations().stream().filter(x -> x.getName().equalsIgnoreCase(location)).findFirst().orElse(null);
        if (locationData == null) return;
        locations.remove(locationData);
        database.deleteAsync("DELETE FROM locations WHERE id = ?", locationData.getId());
    }
}