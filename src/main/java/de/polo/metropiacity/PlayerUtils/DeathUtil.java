package de.polo.metropiacity.PlayerUtils;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.commands.Aduty;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DeathUtil {
    public static final HashMap<String, Boolean> deathPlayer = new HashMap<>();
    public static final HashMap<String, Item> deathSkulls = new HashMap<>();
    public static void startDeathTimer(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (!playerData.isDead()) {
            deathPlayer.put(player.getUniqueId().toString(), true);
            try {
                Statement statement = MySQL.getStatement();
                statement.executeUpdate("UPDATE `players` SET `isDead` = true WHERE `uuid` = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void setHitmanDeath(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setDeathTime(playerData.getDeathTime() + 300);
    }

    public static void setGangwarDeath(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setDeathTime(playerData.getDeathTime() - 180);
    }

    public static void killPlayer(Player player) {
        player.setHealth(0);
    }
    public static void RevivePlayer(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setCanInteract(false);
        playerData.setDead(false);
        playerData.setDeathTime(300);
        deathPlayer.remove(player.getUniqueId().toString());
        Aduty.send_message( player.getName() + " wurde wiederbelebt.");
        player.setFlySpeed(0.1F);
        if (player.isSleeping()) player.wakeup(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 300 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (deathSkulls.get(player.getUniqueId().toString()) != null) {
            Item skull = deathSkulls.get(player.getUniqueId().toString());
            player.teleport(skull.getLocation());
            skull.remove();
            deathSkulls.remove(player.getUniqueId().toString());
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.showPlayer(Main.plugin, player);
        }
        PlayerManager.setPlayerMove(player, true);
        if (playerData.getVariable("gangwar") != null) {
            Gangwar.respawnPlayer(player);
        }
    }

    public static void despawnPlayer(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        deathPlayer.remove(player.getUniqueId().toString());
        player.setGameMode(GameMode.SURVIVAL);
        playerData.setDeathTime(300);
        playerData.setDead(false);
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 300 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (playerData.getVariable("gangwar") != null) {
            Gangwar.respawnPlayer(player);
        } else {
            LocationManager.useLocation(player, "Krankenhaus");
            player.sendMessage(Main.prefix + "Du bist im Krankenhaus aufgewacht.");
            player.getInventory().clear();
        }
        Item skull = deathSkulls.get(player.getUniqueId().toString());
        skull.remove();
        deathSkulls.remove(player.getUniqueId().toString());
        PlayerManager.setPlayerMove(player, true);
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
