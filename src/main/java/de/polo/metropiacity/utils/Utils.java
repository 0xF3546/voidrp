package de.polo.metropiacity.utils;

import de.polo.metropiacity.dataStorage.DBPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.UUID;

public class Utils {
    static int minutes = 1;
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }

    public static void sendBossBar(Player player, String text) {
    }

    public static int getCurrentMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }
    public static int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static OfflinePlayer getOfflinePlayer(String player) {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName().equalsIgnoreCase(player)) {
                return offlinePlayer;
            }
        }
        return null;
    }

    public static String stringArrayToString(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            stringBuilder.append(args[i]);
            if (i != args.length - 1) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }
    public static void sendPlayerAchievementMessage(Player player, String message) {

    }
    public static String toDecimalFormat(int number) {
        return new DecimalFormat("#,###").format(number);
    }
}
