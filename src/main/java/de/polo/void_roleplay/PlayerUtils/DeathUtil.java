package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DeathUtil {
    public static HashMap<String, Boolean> deathPlayer = new HashMap<String, Boolean>();
    public static void startDeathTimer(Player player) {
        deathPlayer.put(player.getUniqueId().toString(), true);
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = true WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void killPlayer(Player player) {
        player.damage(10000);
    }
    public static void RevivePlayer(Player player) {
        deathPlayer.remove(player.getUniqueId().toString());
        PlayerManager.setPlayerMove(player, true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 1440 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        player.sendMessage(Main.prefix + "Du wurdest wiederbelebt.");
    }

    public static void despawnPlayer(Player player) {
        deathPlayer.remove(player.getUniqueId().toString());
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 1440 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LocationManager.useLocation(player, "Krankenhaus");
        player.sendMessage(Main.prefix + "Du bist im Krankenhaus aufgewacht.");
    }

    public static boolean isDead(Player player) throws SQLException {
        Statement statement = MySQL.getStatement();
        assert statement != null;
        String uuid = player.getUniqueId().toString();
        ResultSet result = statement.executeQuery("SELECT `isDead` FROM `players` WHERE `uuid` = '" + uuid + "'");
        boolean res = false;
        if (result.next()) {
            res = result.getBoolean(1);
        }
        return res;
    }
}
