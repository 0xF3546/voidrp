package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.DBPlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

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
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (offlinePlayer.getName().equalsIgnoreCase(player)) {
                return offlinePlayer;
            }
        }
        return null;
    }
}
