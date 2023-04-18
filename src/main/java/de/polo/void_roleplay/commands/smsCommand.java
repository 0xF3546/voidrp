package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PhoneUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class smsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (PhoneUtils.hasPhone(player)) {
            if (args.length >= 2) {
                StringBuilder msg = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) {
                    msg.append(' ').append(args[i]);
                }
                PhoneUtils.sendSMS(player, Integer.parseInt(args[0]), msg);
             } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /sms [Nummer] [Nachricht]");
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
        return false;
    }
}
