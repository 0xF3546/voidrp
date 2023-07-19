package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        String syntax_error = Main.admin_error + "Syntax-Fehler: /ban [name/uuid] [Wert] [Zeit] [Grund]";
        if (playerData.getPermlevel() >= 70) {
            if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("name")) {
                    StringBuilder banreason = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        banreason.append(" ").append(args[i]);
                    }
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        if (players.getName().equalsIgnoreCase(args[1])) {
                            players.kickPlayer("§8• §6§lMetropiaCity §8•\n\n§cDu wurdest vom Server gebannt.\nGrund§8:§7 " + banreason + "\n\n§8• §6§lMetropiaCity §8•");
                        }
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    try {
                        Statement statement = MySQL.getStatement();
                        ResultSet result = statement.executeQuery("SELECT `uuid` FROM `players` WHERE `player_name` = '" + args[1] + "'");
                        if (result.next()) {
                            String uuid = result.getString(1);
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                            if (args[2].contains("h")) {
                                calendar.add(Calendar.HOUR, Integer.parseInt(args[2].replace("h", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + uuid + "', '" + args[1] + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + args[1] + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("d")) {
                                calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(args[2].replace("d", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + uuid + "', '" + args[1] + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + args[1] + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("m")) {
                                calendar.add(Calendar.MONTH, Integer.parseInt(args[2].replace("m", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + uuid + "', '" + args[1] + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + args[1] + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("s")) {
                                calendar.add(Calendar.SECOND, Integer.parseInt(args[2].replace("s", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + uuid + "', '" + args[1] + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + args[1] + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("y")) {
                                calendar.add(Calendar.YEAR, Integer.parseInt(args[2].replace("y", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + uuid + "', '" + args[1] + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + args[1] + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            }
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
                            players.kickPlayer("§8• §6§lMetropiaCity §8•\n\n§cDu wurdest vom Server gebannt.\nGrund§8:§7 " + banreason + "\n\n§8• §6§lMetropiaCity §8•");
                        }
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    try {
                        Statement statement = MySQL.getStatement();
                        ResultSet result = statement.executeQuery("SELECT `player_name` FROM `players` WHERE `uuid` = '" + args[1] + "'");
                        if (result.next()) {
                            String playername = result.getString(1);
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                            if (args[2].contains("h")) {
                                calendar.add(Calendar.HOUR, Integer.parseInt(args[2].replace("h", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + args[1] + "', '" + playername + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + playername + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("d")) {
                                calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(args[2].replace("d", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + args[1] + "', '" + playername + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + playername + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("m")) {
                                calendar.add(Calendar.MONTH, Integer.parseInt(args[2].replace("m", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + args[1] + "', '" + playername + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + playername + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("s")) {
                                calendar.add(Calendar.SECOND, Integer.parseInt(args[2].replace("s", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + args[1] + "', '" + playername + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + playername + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            } else if (args[2].contains("y")) {
                                calendar.add(Calendar.YEAR, Integer.parseInt(args[2].replace("y", "")));
                                Date newDate = calendar.getTime();
                                statement.execute("INSERT INTO `player_bans` (`uuid`, `name`, `reason`, `gebannt`) VALUES ('" + args[1] + "', '" + playername + "', '" + banreason + "', '" + new Date() +"')");
                                Bukkit.broadcastMessage("§c" + playerData.getRang() + " " + player.getName() + " hat " + playername + " gebannt. Grund:" + banreason);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), "§8• §6§lMetropiaCity §8•\n\n§cDu bist vom Server gebannt.\nGrund§8:§7 " + banreason + "\n§cBan läuft ab§8:§7 " + newDate + "\n\n§cEntbannung unter\n§6entbannung@metropiacity.de\n\n§8• §6§lMetropiaCity §8•", newDate, null);
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage(syntax_error);
                }
            } else {
                player.sendMessage(syntax_error);
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
