package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.*;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BusinessManager {
    public static Map<String, BusinessData> businessDataMap = new HashMap<>();
    public static void loadBusinesses() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM business");
        while (locs.next()) {
            BusinessData businessData = new BusinessData();
            businessData.setId(locs.getInt(1));
            businessData.setName(locs.getString(2));
            businessData.setFullname(locs.getString(3));
            businessData.setBank(locs.getInt(4));
            businessDataMap.put(locs.getString(2), businessData);
        }
    }

    public static void setPlayerInBusiness(Player player, String frak, Integer rang) throws SQLException {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        playerData.setBusiness(frak);
        playerData.setBusiness_grade(rang);
        Statement statement = MySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `business` = '" + frak + "', `business_grade` = " + rang + " WHERE `uuid` = '" + uuid + "'");
        boolean found = false;
    }

    public static void removePlayerFromBusiness(Player player) throws SQLException {
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        playerData.setBusiness(null);
        playerData.setBusiness_grade(0);
        Statement statement = MySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `business` = NULL, `business_grade` = 0 WHERE `uuid` = '" + uuid + "'");
    }

    public static void removeOfflinePlayerFromBusiness(String playername) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        ResultSet result = statement.executeQuery(("SELECT * FROM `players` WHERE `player_name` = '" + playername + "'"));
        if (result != null) {
            statement.executeUpdate("UPDATE `players` SET `business` = NULL, `business_grade` = 0 WHERE `player_name` = '" + playername + "'");
        }
    }

    public static String business_offlinePlayer(String playername) throws SQLException {
        String val = null;
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (player.getName().equalsIgnoreCase(playername)) {
                val = dbPlayerData.getFaction();
            }
        }
        return val;
    }
}
