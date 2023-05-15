package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.*;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {
    public static boolean canDoJobsBoolean = true;
    public static String error_cantDoJobs = Main.error + "Der Job ist Serverseitig bis nach Restart gesperrt.";

    public static Map<String, RankData> rankDataMap = new HashMap<>();

    public static Object[][] faction_grades;
    public static void loadRanks() throws SQLException {
        Statement statement = MySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM ranks");
        while (locs.next()) {
            RankData rankData = new RankData();
            rankData.setId(locs.getInt(1));
            rankData.setRang(locs.getString(2));
            rankData.setPermlevel(locs.getInt(3));
            rankData.setTeamSpeakID(locs.getInt(4));
            rankDataMap.put(locs.getString(2), rankData);
        }
    }

    public static boolean canDoJobs() {
        return canDoJobsBoolean;
    }

    public static void savePlayers() throws SQLException {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerManager.savePlayer(player);
        }
    }

    public static void updateTablist(Player player) {
        if (player == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = PlayerManager.playerDataMap.get(p.getUniqueId().toString());
                p.setPlayerListHeader("§6§lVoid Roleplay §8- §cV1.0\n\n§6Bargeld§8: §7" + playerData.getBargeld() + "$\n§6Ping§8:§7 " + p.getPing() + "ms\n");
                p.setPlayerListFooter("\n§6Nachrichten§8:§7 Keine\n§8» §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "§8«");
            }
        } else {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            player.setPlayerListHeader("§6§lVoid Roleplay §8- §cV1.0\n\n§6Bargeld§8: §7" + playerData.getBargeld() + "$\n§6Ping§8:§7 " + player.getPing() + "ms\n");
            player.setPlayerListFooter("\n§6Nachrichten§8:§7 Keine\n§8» §e" + Bukkit.getOnlinePlayers().size() + "§8/§6" + Bukkit.getMaxPlayers() + "§8«");
        }
    }

    public static void startTabUpdateInterval() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTablist(player);
                }
            }
        }.runTaskTimer(Main.getInstance(), 20*2, 20*60);
    }
}
