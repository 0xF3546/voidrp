package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.database.ResultMapper;
import de.polo.voidroleplay.game.base.shops.ShopItem;
import org.bukkit.Material;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Shop {
    public static List<ShopItem> shopItems = new ArrayList<>();
    public static void loadShopItems() throws SQLException {
        ResultMapper<ShopItem> itemMapper = resultSet -> new ShopItem(
                resultSet.getInt("id"),
                resultSet.getInt("shop"),
                Material.valueOf(resultSet.getString("material")),
                resultSet.getString("name"),
                resultSet.getInt("price"),
                resultSet.getString("type"),
                resultSet.getString("type2")
        );

        Main.getInstance().getMySQL().executeQueryAsync("SELECT * FROM shop_items", itemMapper)
                .thenAccept(items -> {
                    shopItems = items;
                });
    }
}
