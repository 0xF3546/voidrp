package de.polo.metropiacity.utils;

import de.polo.metropiacity.dataStorage.DBPlayerData;
import de.polo.metropiacity.dataStorage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public static LocalDateTime toLocalDateTime(Date date) {
        long newDate = date.getTime();
        java.util.Date utilDate = new java.util.Date(newDate);

        LocalDateTime localDateTime = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime;
    }
    public interface Tablist {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Team team = scoreboard.registerNewTeam("a");
        Team team_offduty = scoreboard.registerNewTeam("b");
        Team FBI = scoreboard.registerNewTeam("c");
        Team Polizei = scoreboard.registerNewTeam("d");
        Team Medics = scoreboard.registerNewTeam("e");
        static void updatePlayer(Player player) {
            team.removeEntry(player.getName());
            team_offduty.removeEntry(player.getName());
            FBI.removeEntry(player.getName());
            Polizei.removeEntry(player.getName());
            Medics.removeEntry(player.getName());
            PlayerData playerData = PlayerManager.getPlayerData(player);
            if (playerData.isAduty()) {
                team.addEntry(player.getName());
                return;
            }
            if (playerData.isDuty()) {
                switch (playerData.getFaction().toLowerCase()) {
                    case "polizei":
                        Polizei.addEntry(player.getName());
                        break;
                    case "fbi":
                        FBI.addEntry(player.getName());
                    case "medic":
                        Medics.addEntry(player.getName());
                        break;
                    default:
                        team_offduty.addEntry(player.getName());
                        break;
                }
            }
        }
    }
}
