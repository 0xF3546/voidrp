package de.polo.metropiacity.playerUtils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.ShopItem;
import de.polo.metropiacity.database.MySQL;
import org.bukkit.Material;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Shop {
    public static List<ShopItem> shopItems = new ArrayList<>();
    public static void loadShopItems() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM shop_items");
        List<Object[]> resultList = new ArrayList<>();
        while (locs.next()) {
            ShopItem item = new ShopItem();
            item.setId(locs.getInt("id"));
            item.setShop(locs.getInt("shop"));
            item.setMaterial(Material.valueOf(locs.getString("material")));
            item.setDisplayName(locs.getString("name"));
            item.setPrice(locs.getInt("price"));
            item.setType(locs.getString("type"));
            item.setSecondType(locs.getString("type2"));
            shopItems.add(item);
        }
    }
}
