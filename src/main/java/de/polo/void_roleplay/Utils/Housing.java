package de.polo.void_roleplay.Utils;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Housing {
    public static Map<Integer, HouseData> houseDataMap = new HashMap<>();
    public static void loadHousing() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM housing");
        while (locs.next()) {
            HouseData houseData = new HouseData();
            houseData.setId(locs.getInt(1));
            houseData.setOwner(locs.getString(2));
            houseData.setNumber(locs.getInt(3));
            houseData.setPrice(locs.getInt(4));
            houseData.setTotalMoney(locs.getInt(7));

            JSONObject object = new JSONObject(locs.getString(5));
            HashMap<String, Integer> map = new HashMap<>();
            for (String key : object.keySet()) {
                int value = (int) object.get(key);
                map.put(key, value);
            }
            houseData.setRenter(map);
            houseData.setMoney(locs.getInt(6));
            houseDataMap.put(locs.getInt(3), houseData);
        }
    }

    public static boolean isPlayerOwner(Player player, int number) {
        HouseData houseData = houseDataMap.get(number);
        if (player.getUniqueId().toString().equals(houseData.getOwner())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean canPlayerInteract(Player player, int number) {
        HouseData houseData = houseDataMap.get(number);
        if (!Objects.equals(houseData.getOwner(), player.getUniqueId().toString())) {
            System.out.println("spieler ist kein owner");
            System.out.println(houseData.getRenter().get(player.getUniqueId().toString()));
            if (houseData.getRenter().get(player.getUniqueId().toString()) != null) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }
    public static void updateRenter(int number) {
        HouseData houseData = houseDataMap.get(number);
        try {
            Statement statement = MySQL.getStatement();
            JSONObject object = new JSONObject(houseData.getRenter());
            statement.executeUpdate("UPDATE `housing` SET `renter` = '" + object + "' WHERE `number` = " + number);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addHausSlot(Player player) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        System.out.println("hausslot hinzugefügt");
        playerData.setHouseSlot(playerData.getHouseSlot() + 1);
        Statement statement = MySQL.getStatement();
        statement.executeUpdate("UPDATE `players` SET `houseSlot` = " + playerData.getHouseSlot() + " WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
    }

    public static boolean resetHouse(Player player, int house) {
        for (HouseData houseData : houseDataMap.values()) {
            int centerX = player.getLocation().getBlockX();
            int centerY = player.getLocation().getBlockY();
            int centerZ = player.getLocation().getBlockZ();
            World world = player.getWorld();
            for (int x = centerX - 6; x <= centerX + 6; x++) {
                for (int y = centerY - 6; y <= centerY + 6; y++) {
                    for (int z = centerZ - 6; z <= centerZ + 6; z++) {
                        Location location = new Location(world, x, y, z);
                        Block block = location.getBlock();
                        if (block.getType().toString().contains("SIGN")) {
                            Sign sign = (Sign) block.getState();
                            NamespacedKey value = new NamespacedKey(Main.plugin, "value");
                            PersistentDataContainer container = new CustomBlockData(block, Main.plugin);
                            System.out.println(container.get(value, PersistentDataType.INTEGER));
                            if (container.get(value, PersistentDataType.INTEGER) != null) {
                                if (house == Objects.requireNonNull(container.get(value, PersistentDataType.INTEGER))) {
                                    if (houseData.getNumber() == house) {
                                        houseData.setOwner(null);
                                        try {
                                            Statement statement = MySQL.getStatement();
                                            statement.executeUpdate("UPDATE `housing` SET `owner` = null WHERE `number` = " + house);
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
