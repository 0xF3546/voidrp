package de.polo.metropiacity.utils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.database.MySQL;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class BusinessManager {
    private final PlayerManager playerManager;
    private final List<BusinessData> businesses = new ArrayList<>();
    public BusinessManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        try {
            loadBusinesses();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadBusinesses() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM business");
        while (locs.next()) {
            BusinessData businessData = new BusinessData();
            businessData.setId(locs.getInt("id"));
            businessData.setOwner(UUID.fromString(locs.getString("owner")));
            businessData.setName(locs.getString("name"));
            businessData.setFullname(locs.getString("fullname"));
            businessData.setBank(locs.getInt("bank"));
            businessData.setMaxMember(25);
            businessData.setActive(locs.getBoolean("activated"));
            businesses.add(businessData);
        }
    }

    public void removePlayerFromBusiness(Player player) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setBusiness(null);
        playerData.setBusiness_grade(0);
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        statement.executeUpdate("UPDATE `players` SET `business` = NULL, `business_grade` = 0 WHERE `uuid` = '" + player.getUniqueId() + "'");
    }

    public BusinessData getBusinessData(int id) {
        for (BusinessData businessData : businesses) {
            if (businessData.getId() == id) return businessData;
        }
        return null;
    }

    public static void removeOfflinePlayerFromBusiness(String playername) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        assert statement != null;
        ResultSet result = statement.executeQuery(("SELECT * FROM `players` WHERE `player_name` = '" + playername + "'"));
        if (result != null) {
            statement.executeUpdate("UPDATE `players` SET `business` = NULL, `business_grade` = 0 WHERE `player_name` = '" + playername + "'");
        }
    }

    public static String business_offlinePlayer(String playername) {
        String val = null;
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (player.getName().equalsIgnoreCase(playername)) {
                val = dbPlayerData.getFaction();
            }
        }
        return val;
    }
    public static int getMemberCount(int business) {
        int count = 0;
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            if (dbPlayerData.getBusiness().equals(business)) {
                count++;
            }
        }
        return count;
    }
    @SneakyThrows
    public int createBusiness(BusinessData businessData) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO business (owner, activated) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, businessData.getOwner().toString());
        statement.setBoolean(2, businessData.isActive());
        int affectedRows = statement.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Creating business failed, no rows affected.");
        }

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                businessData.setId(generatedKeys.getInt(1));
                businesses.add(businessData);
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating business failed, no ID obtained.");
            }
        }
    }

}
