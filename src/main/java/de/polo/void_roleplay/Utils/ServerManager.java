package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
}
