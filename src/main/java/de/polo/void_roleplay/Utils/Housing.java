package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.entity.Player;
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
        playerData.setHouseSlot(playerData.getHouseSlot() + 1);
        Statement statement = MySQL.getStatement();
        statement.executeUpdate("UPDATE `players` SET `houseSlot` = " + playerData.getHouseSlot() + " WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
    }
}
