package de.polo.metropiacity.playerUtils;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.AdminManager;
import de.polo.metropiacity.utils.Game.GangwarUtils;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DeathUtils {
    private final HashMap<String, Boolean> deathPlayer = new HashMap<>();
    private final HashMap<String, Item> deathSkulls = new HashMap<>();
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final LocationManager locationManager;
    public DeathUtils(PlayerManager playerManager, AdminManager adminManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.locationManager = locationManager;
    }

    public Item getDeathSkull(String UUID) {
        return deathSkulls.get(UUID);
    }

    public void addDeathSkull(String UUID, Item item) {
        deathSkulls.put(UUID, item);
    }

    public void removeDeathSkull(String UUID) {
        deathSkulls.remove(UUID);
    }
    public void startDeathTimer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isDead()) {
            deathPlayer.put(player.getUniqueId().toString(), true);
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE `players` SET `isDead` = true WHERE `uuid` = '" + player.getUniqueId() + "'");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setHitmanDeath(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setDeathTime(playerData.getDeathTime() + 300);
    }

    public void setGangwarDeath(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setDeathTime(playerData.getDeathTime() - 180);
    }

    public void killPlayer(Player player) {
        player.setHealth(0);
    }
    public void RevivePlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setCanInteract(false);
        playerData.setDead(false);
        playerData.setDeathTime(300);
        deathPlayer.remove(player.getUniqueId().toString());
        adminManager.send_message( player.getName() + " wurde wiederbelebt.", null);
        player.setFlySpeed(0.1F);
        if (player.isSleeping()) player.wakeup(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
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
        playerManager.setPlayerMove(player, true);
        if (playerData.getVariable("gangwar") != null) {
            Main.getInstance().gangwarUtils.respawnPlayer(player);
        }
    }

    public void despawnPlayer(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        deathPlayer.remove(player.getUniqueId().toString());
        player.setGameMode(GameMode.SURVIVAL);
        playerData.setDeathTime(300);
        playerData.setDead(false);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 300 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (playerData.getVariable("gangwar") != null) {
            Main.getInstance().gangwarUtils.respawnPlayer(player);
        } else {
            locationManager.useLocation(player, "Krankenhaus");
            player.sendMessage(Main.prefix + "Du bist im Krankenhaus aufgewacht.");
            player.getInventory().clear();
        }
        Item skull = deathSkulls.get(player.getUniqueId().toString());
        skull.remove();
        deathSkulls.remove(player.getUniqueId().toString());
        playerManager.setPlayerMove(player, true);
    }

    public static boolean isDead(Player player) throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
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
