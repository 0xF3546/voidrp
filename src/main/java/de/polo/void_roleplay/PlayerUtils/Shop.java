package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.MySQl.MySQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Shop {
    public static Object[][] shop_items;
    public static void loadShopItems() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM shop_items");
        List<Object[]> resultList = new ArrayList<>();
        while (locs.next()) {
            Object[] row = new Object[7];
            row[0] = locs.getInt(1);
            row[1] = locs.getInt(2);
            row[2] = locs.getString(3);
            row[3] = locs.getString(4);
            row[4] = locs.getInt(5);
            row[5] = locs.getString(6);
            row[6] = locs.getString(7);
            resultList.add(row);
        }
        shop_items = new Object[resultList.size()][];
        for (int i = 0; i < resultList.size(); i++) {
            shop_items[i] = resultList.get(i);
        }
    }
}
