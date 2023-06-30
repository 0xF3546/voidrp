package de.polo.metropiacity.PlayerUtils;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.commands.aduty;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DeathUtil {
    public static HashMap<String, Boolean> deathPlayer = new HashMap<String, Boolean>();
    public static HashMap<String, Item> deathSkulls = new HashMap<>();
    public static void startDeathTimer(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (!playerData.isDead()) {
            deathPlayer.put(player.getUniqueId().toString(), true);
            try {
                Statement statement = MySQL.getStatement();
                statement.executeUpdate("UPDATE `players` SET `isDead` = true WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (!playerData.isDead()) cancel();
                            playerData.setDeathTime(playerData.getDeathTime() - 1);
                            String actionBarText = "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot.";
                            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                            if (playerData.getDeathTime() <= 0) {
                                playerData.setDeathTime(600);
                                despawnPlayer(players);
                                cancel();
                            }
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 20, 20);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        PlayerData playerData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                        if (!playerData.isDead()) cancel();
                        playerData.setDeathTime(playerData.getDeathTime() - 1);
                        String actionBarText = "§cDu bist noch " + Main.getTime(playerData.getDeathTime()) + " Tot.";
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                        if (playerData.getDeathTime() <= 0) {
                            playerData.setDeathTime(600);
                            despawnPlayer(players);
                            cancel();
                        }
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);
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
        deathPlayer.remove(player.getUniqueId().toString());
        aduty.send_message( player.getName() + " wurde wiederbelebt.");
        if (player.isSleeping()) player.wakeup(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 600 WHERE `uuid` = '" + uuid + "'");
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
            player.sendMessage("§8[§cGangwar§8]§a Du bist respawnt.");
        } else {
            player.sendMessage(Main.prefix + "Du wurdest wiederbelebt.");
        }
    }

    public static void despawnPlayer(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        deathPlayer.remove(player.getUniqueId().toString());
        try {
            Statement statement = MySQL.getStatement();
            assert statement != null;
            String uuid = player.getUniqueId().toString();
            statement.executeUpdate("UPDATE `players` SET `isDead` = false, `deathTime` = 600 WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (playerData.getVariable("gangwar") != null) {
            Gangwar.respawnPlayer(player);
            player.sendMessage("§8[§cGangwar§8]§a Du bist respawnt.");
        } else {
            LocationManager.useLocation(player, "Krankenhaus");
            player.sendMessage(Main.prefix + "Du bist im Krankenhaus aufgewacht.");
            player.getInventory().clear();
        }
        Item skull = deathSkulls.get(player.getUniqueId().toString());
        skull.remove();
        deathSkulls.remove(player.getUniqueId().toString());
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.showPlayer(Main.plugin, player);
        }
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
