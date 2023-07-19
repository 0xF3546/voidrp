package de.polo.metropiacity.playerUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatUtils {
    public static void sendMeMessageAtPlayer(Player player, String message) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(players.getLocation()) <= 5) {
                players.sendMessage("§8► §a" + message);
            }
        }
    }

    public static void sendGrayMessageAtPlayer(Player player, String message) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(players.getLocation()) <= 5) {
                players.sendMessage("§8➥ §7" + message);
            }
        }
    }
}
