package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class ServerManager {
    public static boolean canDoJobsBoolean = true;
    public static String error_cantDoJobs = Main.error + "Der Job ist Serverseitig bis nach Restart gesperrt.";

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
