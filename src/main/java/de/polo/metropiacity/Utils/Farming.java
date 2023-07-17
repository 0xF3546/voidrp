package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.FarmingData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.Events.BreakPersistentBlockEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Farming implements Listener {
    public static Map<String, FarmingData> farmingDataMap = new HashMap<>();
    public static void loadData() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet res = statement.executeQuery("SELECT * FROM farming");
        while (res.next()) {
            FarmingData farmingData = new FarmingData();
            farmingData.setId(res.getInt(1));
            farmingData.setFarmer(res.getBoolean(2));
            farmingData.setType(res.getString(3));
            farmingData.setAmount(res.getInt(4));
            farmingData.setDuration(res.getInt(5));
            farmingData.setNeeded_item(res.getString(6));
            farmingData.setItem(Material.valueOf(res.getString(7)));
            farmingData.setItemName(res.getString(8));
            farmingDataMap.put(res.getString(3), farmingData);
        }
    }
    @EventHandler
    public void onPersistentBlockBreak(BreakPersistentBlockEvent event) {
        String type = event.getPersistentData(PersistentDataType.STRING);
        if (type.contains("farming_")) {
            String farmingType = type.replace("farming_", "");
            FarmingData farmingData = farmingDataMap.get(farmingType);
            if (farmingData == null) return;
            Player player = event.getPlayer();
            if (Main.cooldownManager.isOnCooldown(player, "farming")) return;
            player.getInventory().addItem(ItemManager.createItem(farmingData.getItem(), farmingData.getAmount(), 0, farmingData.getItemName(), null));
            Main.cooldownManager.setCooldown(player, "farming", farmingData.getDuration());
        }
    }
}
