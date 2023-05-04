package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.GasStationData;
import de.polo.void_roleplay.DataStorage.LocationData;
import de.polo.void_roleplay.DataStorage.NaviData;
import de.polo.void_roleplay.DataStorage.PlayerVehicleData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class LocationManager {
    public static Map<String, LocationData> locationDataMap = new HashMap<String, LocationData>();
    public static Map<Integer, GasStationData> gasStationDataMap = new HashMap<>();
    public static Map<Integer, NaviData> naviDataMap = new HashMap<>();
    public static Object[][] shops;
    public static Object[][] gasstations;
    public static void loadLocations() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM locations");
        while (locs.next()) {
            LocationData locationData = new LocationData();
            locationData.setId(locs.getInt(1));
            locationData.setName(locs.getString(2).toLowerCase());
            locationData.setX(locs.getInt(3));
            locationData.setY(locs.getInt(4));
            locationData.setZ(locs.getInt(5));
            locationData.setWelt(locs.getString(6));
            locationData.setYaw(locs.getFloat(7));
            locationData.setPitch(locs.getFloat(8));
            locationDataMap.put(locs.getString(2), locationData);
        }

        ResultSet shop = statement.executeQuery("SELECT * FROM shops");
        List<Object[]> shopList = new ArrayList<>();
        while (shop.next()) {
            Object[] row = new Object[8];
            row[0] = shop.getInt(1);
            row[1] = shop.getString(2);
            row[2] = shop.getInt(3);
            row[3] = shop.getInt(4);
            row[4] = shop.getInt(5);
            row[5] = shop.getString(6);
            row[6] = shop.getFloat(7);
            row[7] = shop.getFloat(8);
            shopList.add(row);
        }
        shops = new Object[shopList.size()][];
        for (int i = 0; i < shopList.size(); i++) {
            shops[i] = shopList.get(i);
        }

        ResultSet gas = statement.executeQuery("SELECT * FROM gasstations");
        while (gas.next()) {
            GasStationData gasStationData = new GasStationData();
            gasStationData.setId(gas.getInt(1));
            gasStationData.setName(gas.getString(2));
            gasStationData.setX(gas.getInt(3));
            gasStationData.setY(gas.getInt(4));
            gasStationData.setZ(gas.getInt(5));
            gasStationData.setWelt(Bukkit.getWorld(gas.getString(6)));
            gasStationData.setYaw(gas.getFloat(7));
            gasStationData.setPitch(gas.getFloat(8));
            gasStationData.setUuid(gas.getString(9));
            gasStationData.setPrice(gas.getInt(10));
            gasStationData.setLiterprice(gas.getInt(11));
            gasStationData.setLiter(gas.getInt(12));
            gasStationDataMap.put(gas.getInt(1), gasStationData);
        }

        ResultSet navi = statement.executeQuery("SELECT * FROM navi");
        while (navi.next()) {
            NaviData naviData = new NaviData();
            naviData.setId(navi.getInt(1));
            naviData.setisGroup(navi.getBoolean(2));
            naviData.setGroup(navi.getString(3));
            naviData.setName(navi.getString(4));
            if (navi.getString(5) != null) naviData.setLocation(navi.getString(5));
            naviData.setItem(Material.valueOf(navi.getString(6)));
            naviDataMap.put(navi.getInt(1), naviData);
        }

    }

    public static void setLocation(String name, Player p){
        Location loc = p.getLocation();
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            if (name.contains("isShop")) {
                p.sendMessage(Main.gamedesign_prefix + " Shop regestriert.");
                statement.executeUpdate("INSERT INTO shops (name, x, y, z, welt, yaw, pitch) VALUES ('" + name.replace("isShop", "") + "', " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ", '" + loc.getWorld().getName() + "', " + loc.getYaw() + ", " + loc.getPitch() + ");");
            }  else if (name.contains("isGas")) {
                p.sendMessage(Main.gamedesign_prefix + " Tankstelle regestriert.");
                statement.executeUpdate("INSERT INTO gasstations (name, x, y, z, welt, yaw, pitch) VALUES ('" + name.replace("isGas", "") + "', " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ", '" + loc.getWorld().getName() + "', " + loc.getYaw() + ", " + loc.getPitch() + ");");
            } else {
                statement.executeUpdate("INSERT INTO locations (name, x, y, z, welt, yaw, pitch) VALUES ('" + name + "', " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ", '" + loc.getWorld().getName() + "', " + loc.getYaw() + ", " + loc.getPitch() + ");");
            }
            } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void useLocation(Player p, String name){
        LocationData locationData = locationDataMap.get(" " + name.toLowerCase());
        World welt = Bukkit.getWorld(locationData.getWelt());
        p.teleport(new Location(welt, locationData.getX(), locationData.getY(), locationData.getZ(), (float) locationData.getYaw(), (float) locationData.getPitch()));
    }

    public static double getDistanceBetweenCoords(Player player, String name) {
        LocationData locationData = locationDataMap.get(" " + name.toLowerCase());
        World welt = Bukkit.getWorld(locationData.getWelt());
        return player.getLocation().distance(new Location(welt, locationData.getX(), locationData.getY(), locationData.getZ(), (float) locationData.getYaw(), (float) locationData.getPitch()));
    }

    public static int isNearShop(Player player) {
        int distshop = 0;
        for (Object[] row : shops) {
            World welt = Bukkit.getWorld(row[5].toString());
            if (player.getLocation().distance(new Location(welt, (int) row[2], (int) row[3], (int) row[4], (float) row[6], (float) row[7])) < 4) {
                distshop = (int) row[0];
            }
        }
        return distshop;
    }

    public static String getShopNameById(Integer id) {
        for (Object[] row : shops) {
            if (row[0] == id) {
                System.out.println((String) row[1]);
                return (String) row[1];
            }
        }
        return null;
    }

    public static boolean nearATM(Player player) {
        boolean returnval = false;
        int centerX = player.getLocation().getBlockX();
        int centerY = player.getLocation().getBlockY();
        int centerZ = player.getLocation().getBlockZ();
        World world = player.getWorld();
        for (int x = centerX - 3; x <= centerX + 3; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - 3; z <= centerZ + 3; z++) {
                    Location location = new Location(world, x, y, z);
                    Block block = location.getBlock();
                    if (block.getType().toString().contains("SIGN")) {
                        Sign sign = (Sign) block.getState();
                        System.out.println(sign.getLine(1));
                        if (sign.getLine(1).contains("Bankautomat")) {
                            returnval = true;
                            System.out.println("Atm gefunden");
                        }
                    }
                }
            }
        }
        return returnval;
    }

    public static Integer isPlayerNearOwnHouse(Player player) {
        int centerX = player.getLocation().getBlockX();
        int centerY = player.getLocation().getBlockY();
        int centerZ = player.getLocation().getBlockZ();
        World world = player.getWorld();
        for (int x = centerX - 3; x <= centerX + 3; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - 3; z <= centerZ + 3; z++) {
                    Location location = new Location(world, x, y, z);
                    Block block2 = location.getBlock();
                    if (block2.getType().toString().contains("SIGN")) {
                        System.out.println("sign found");
                        Sign sign = (Sign) block2.getState();
                        if (!sign.getLine(2).contains(player.getName())) {
                            Integer number = Integer.valueOf(sign.getLine(0).replace("*- Haus", "").replace("-*", ""));
                            return number;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public static Integer isPlayerGasStation(Player player) {
        for (GasStationData gasStationData : gasStationDataMap.values()) {
            if (player.getLocation().distance(new Location(gasStationData.getWelt(), gasStationData.getX(), gasStationData.getY(), gasStationData.getZ(), gasStationData.getYaw(), gasStationData.getPitch())) < 25) {
                return gasStationData.getId();
            }
        }
        return 0;
    }

}
