package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatUtils {
    public static void sendMeMessageAtPlayer(Player player, String message) {
        Bukkit.getOnlinePlayers()
                .parallelStream()
                .filter(p -> p.getLocation().distance(player.getLocation()) <= 5)
                .forEach(p -> p.sendMessage("§8► §a" + message));
    }

    public static void sendGrayMessageAtPlayer(Player player, String message) {
        Bukkit.getOnlinePlayers()
                .parallelStream()
                .filter(p -> p.getLocation().distance(player.getLocation()) <= 5)
                .forEach(p -> p.sendMessage("§8➥ §7" + message));
    }

    public static void logMessage(String message, UUID uuid) {
        Main.getInstance()
                .getCoreDatabase()
                .insertAsync("INSERT INTO chat_logs (uuid, message) VALUES (?, ?)", uuid.toString(), message);
    }

    public static void logCommand(String command, UUID uuid) {
        Main.getInstance()
                .getCoreDatabase()
                .insertAsync("INSERT INTO command_logs (uuid, command) VALUES (?, ?)", uuid.toString(), command);
    }
}
