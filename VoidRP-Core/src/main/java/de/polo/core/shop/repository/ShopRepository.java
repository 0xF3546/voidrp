package de.polo.core.shop.repository;

import de.polo.core.Main;
import de.polo.core.game.base.shops.ShopData;
import de.polo.core.game.base.shops.ShopItem;
import de.polo.core.utils.enums.ShopType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static de.polo.core.Main.database;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShopRepository {
    @SneakyThrows
    public List<ShopData> loadShops() {
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM shops");
        List<ShopData> shopDataList = new ObjectArrayList<>();
        while (locs.next()) {
            ShopData shopData = new ShopData();
            shopData.setId(locs.getInt(1));
            shopData.setName(locs.getString(2));
            shopData.setX(locs.getInt(3));
            shopData.setY(locs.getInt(4));
            shopData.setZ(locs.getInt(5));
            shopData.setWelt(Bukkit.getWorld(locs.getString(6)));
            shopData.setYaw(locs.getFloat(7));
            shopData.setPitch(locs.getFloat(8));
            shopData.setBank(locs.getInt("bank"));
            if (locs.getInt("company") != 0) {
                shopData.setCompany(locs.getInt("company"));
            }
            shopData.setCrewHolder(locs.getInt("crew"));

            String typeString = locs.getString(9);
            if (typeString != null) {
                try {
                    ShopType type = ShopType.valueOf(typeString.toUpperCase());
                    shopData.setType(type);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid shop type: " + typeString);
                    continue;
                }
            } else {
                System.err.println("Null shop type retrieved from the database");
                continue;
            }
            shopDataList.add(shopData);
            try {
                Statement nStatement = Main.getInstance().coreDatabase.getStatement();
                ResultSet i = nStatement.executeQuery("SELECT * FROM shop_items WHERE shop = " + shopData.getId());
                while (i.next()) {
                    ShopItem item = new ShopItem();
                    item.setId(i.getInt("id"));
                    item.setShop(i.getInt("shop"));
                    item.setMaterial(Material.valueOf(i.getString("material")));
                    item.setDisplayName(i.getString("name"));
                    item.setPrice(i.getInt("price"));
                    item.setType(i.getString("type"));
                    item.setSecondType(i.getString("type2"));
                    shopData.addItem(item);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        statement.close();
        return shopDataList;
    }

    public void saveShop(ShopData shop) {
        database.updateAsync("UPDATE shops SET name = ?, x = ?, y = ?, z = ?, world = ?, yaw = ?, pitch = ?, bank = ?, company = ?, crew = ? WHERE id = ?",
                shop.getName(), shop.getX(), shop.getY(), shop.getZ(), shop.getWelt().getName(), shop.getYaw(), shop.getPitch(), shop.getBank(), shop.getCompany(), shop.getCrewHolder(), shop.getId());
    }
}
