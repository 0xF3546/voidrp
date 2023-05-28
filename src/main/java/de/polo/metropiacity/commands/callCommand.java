package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.PhoneUtils;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class callCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (PhoneUtils.hasPhone(player)) {
            if (!playerData.isFlightmode()) {
                if (args.length >= 1) {
                    if (Integer.parseInt(args[0]) != 0) {
                        if (!Objects.equals(playerData.getVariable("calling"), "Ja")) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData targetplayerData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                                if (targetplayerData.getNumber() == Integer.parseInt(args[0])) {
                                    if (PhoneUtils.getConnection(players) == null) {
                                        if (!targetplayerData.isFlightmode()) {
                                            try {
                                                if (playerData.getNumber() == 0) {
                                                    player.sendMessage("§8[§6Handy§8]&a Deine Nummer lautet nun " + playerData.getId());
                                                    playerData.setNumber(playerData.getId());
                                                    Statement statement = MySQL.getStatement();
                                                    statement.executeUpdate("UPDATE `players` SET `number` = " + playerData.getId() + " WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                                                }
                                                PhoneUtils.callNumber(player, targetplayerData.getNumber());
                                            } catch (SQLException e) {
                                                player.sendMessage(Main.error + "Ein Fehler ist aufgetreten. Kontaktiere einen Entwickler.");
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            player.sendMessage(Main.error + players.getName() + " ist nicht erreichbar.");
                                        }
                                    } else {
                                        player.sendMessage(Main.error + players.getName() + " ist bereits in einem Gespräch.");
                                    }
                                }
                            }
                        } else {
                            player.sendMessage(Main.error + "Du rufst bereits jemanden an.");
                        }
                    } else {
                        player.sendMessage(Main.error + "Die Nummer muss größer als 0 sein");
                    }
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /call [Nummer]");
                }
            } else {
                player.sendMessage(PhoneUtils.error_flightmode);
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
        return false;
    }
}
