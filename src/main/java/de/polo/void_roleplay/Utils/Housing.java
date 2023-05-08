package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.entity.Player;
import org.json.JSONArray;

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
            JSONArray array = new JSONArray(locs.getString(5));
            List<String> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
            houseData.setRenter(list);
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
            for (int i = 0; i < houseData.getRenter().size(); i++) {
                if (player.getUniqueId().toString().equals(houseData.getRenter().get(i))) {
                    return true;
                }
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
            JSONArray array = new JSONArray(houseData.getRenter());
            statement.executeUpdate("UPDATE `housing` SET `renter` = '" + array + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
