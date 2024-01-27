package de.polo.metropiacity.utils.playerUtils;

import de.polo.metropiacity.Main;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.util.UUID;

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

    @SneakyThrows
    public static void LogMessage(String message, UUID uuid) {
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("INSERT INTO chat_logs (uuid, message) VALUES (?, ?)");
        statement.setString(1, uuid.toString());
        statement.setString(2, message);
        statement.execute();
    }

    @SneakyThrows
    public static void LogCommand(String command, UUID uuid) {
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("INSERT INTO command_logs (uuid, command) VALUES (?, ?)");
        statement.setString(1, uuid.toString());
        statement.setString(2, command);
        statement.execute();
    }
}
