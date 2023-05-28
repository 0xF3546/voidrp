package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.PhoneUtils;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class smsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (PhoneUtils.hasPhone(player)) {
            if (!playerData.isFlightmode()) {
                if (args.length >= 2) {
                    if (Integer.parseInt(args[0]) != 0) {
                        StringBuilder msg = new StringBuilder(args[1]);
                        for (int i = 2; i < args.length; i++) {
                            msg.append(' ').append(args[i]);
                        }
                        if (playerData.getNumber() == 0) {
                            player.sendMessage("§8[§6Handy§8]&a Deine Nummer lautet nun " + playerData.getId());
                            playerData.setNumber(playerData.getId());
                            try {
                                Statement statement = MySQL.getStatement();
                                statement.executeUpdate("UPDATE `players` SET `number` = " + playerData.getId() + " WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        PhoneUtils.sendSMS(player, Integer.parseInt(args[0]), msg);
                    } else {
                        player.sendMessage(Main.error + "Die Nummer muss größer als 0 sein");
                    }
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /sms [Nummer] [Nachricht]");
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
