package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        String syntax_error = Main.admin_error + "Syntax-Fehler: /ban [name/uuid] [Wert] [Zeit] [Grund]";
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 3) {
            player.sendMessage(syntax_error);
            return false;
        }
        String targetName = null;
        String BanDuration = args[2].toLowerCase();
        if (args[0].equalsIgnoreCase("name")) {
            StringBuilder banreason = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                banreason.append(" ").append(args[i]);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (players.getName().equalsIgnoreCase(args[1])) {
                    try {
                        PlayerManager.savePlayer(players);
                        players.closeInventory();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    players.kickPlayer("§8• §6§lMetropiaCity §8•\n\n§cDu wurdest vom Server gebannt.\nGrund§8:§7 " + banreason + "\n\n§8• §6§lMetropiaCity §8•");
                }
            }
            try {
                Statement statement = MySQL.getStatement();
                ResultSet result = statement.executeQuery("SELECT `uuid` FROM `players` WHERE `player_name` = '" + args[1] + "'");
                if (result.next()) {
                    String uuid = result.getString(1);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    LocalDateTime localDateTime = LocalDateTime.now();
                    if (BanDuration.contains("h")) {
                        localDateTime = localDateTime.plusHours(Integer.parseInt(BanDuration.replace("h", "")));
                    } else if (BanDuration.contains("d")) {
                        localDateTime = localDateTime.plusDays(Integer.parseInt(BanDuration.replace("d", "")));
                    } else if (BanDuration.contains("m")) {
                        localDateTime = localDateTime.plusMonths(Integer.parseInt(BanDuration.replace("m", "")));
                    } else if (BanDuration.contains("s")) {
                        localDateTime = localDateTime.plusSeconds(Integer.parseInt(BanDuration.replace("s", "")));
                    } else if (BanDuration.contains("y")) {
                        localDateTime = localDateTime.plusYears(Integer.parseInt(BanDuration.replace("y", "")));
                    }
                    statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `punisher`, `date`) VALUES ('" + uuid + "', '" + args[1] + "', '" + banreason + "', '" + player.getName() + "', '" + localDateTime + "')");
                    targetName = args[1];
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (args[0].equalsIgnoreCase("uuid")) {
            StringBuilder banreason = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                banreason.append(" ").append(args[i]);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (players.getUniqueId().toString().equalsIgnoreCase(args[1])) {
                    try {
                        PlayerManager.savePlayer(players);
                        players.closeInventory();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    players.kickPlayer("§8• §6§lMetropiaCity §8•\n\n§cDu wurdest vom Server gebannt.\nGrund§8:§7 " + banreason + "\n\n§8• §6§lMetropiaCity §8•");
                }
            }
            try {
                Statement statement = MySQL.getStatement();
                ResultSet result = statement.executeQuery("SELECT `player_name` FROM `players` WHERE `uuid` = '" + args[1] + "'");
                if (result.next()) {
                    String playername = result.getString(1);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                    LocalDateTime localDateTime = LocalDateTime.now();
                    if (BanDuration.contains("h")) {
                        localDateTime = localDateTime.plusHours(Integer.parseInt(BanDuration.replace("h", "")));
                    } else if (BanDuration.contains("d")) {
                        localDateTime = localDateTime.plusDays(Integer.parseInt(BanDuration.replace("d", "")));
                    } else if (BanDuration.contains("m")) {
                        localDateTime = localDateTime.plusMonths(Integer.parseInt(BanDuration.replace("m", "")));
                    } else if (BanDuration.contains("s")) {
                        localDateTime = localDateTime.plusSeconds(Integer.parseInt(BanDuration.replace("s", "")));
                    } else if (BanDuration.contains("y")) {
                        localDateTime = localDateTime.plusYears(Integer.parseInt(BanDuration.replace("y", "")));
                    }
                    statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `punisher`, `date`) VALUES ('" + args[1] + "', '" + result.getString(1) + "', '" + banreason + "', '" + player.getName() + "', '" + localDateTime + "')");
                    targetName = playername;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage(syntax_error);
            return false;
        }
        Bukkit.broadcastMessage(ChatColor.RED + playerData.getRang() + " " + player.getName() + " hat " + targetName + " gebannt. Grnd: " + args[3]);
        return false;
    }
}
