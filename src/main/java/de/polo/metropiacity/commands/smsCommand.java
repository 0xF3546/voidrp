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

public class smsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (PhoneUtils.hasPhone(player)) {
            if (!playerData.isFlightmode()) {
                if (args.length >= 2) {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                        StringBuilder msg = new StringBuilder(args[1]);
                        for (int i = 2; i < args.length; i++) {
                            msg.append(' ').append(args[i]);
                        }
                        PhoneUtils.sendSMS(player, targetplayer, msg);
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /sms [Spieler] [Nachricht]");
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
