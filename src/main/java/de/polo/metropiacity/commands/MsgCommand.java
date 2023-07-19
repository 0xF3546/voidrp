package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MsgCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 40) {
            if (args.length >= 2) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                String msg = args[1];
                for (int i = 2; i < args.length; i++) {
                    msg = msg + " " + args[i];
                }
                targetplayer.sendMessage("§d" + playerData.getRang() + " " + player.getName() + " zu dir: " + msg);
                player.sendMessage("§dDu zu " + targetplayer.getName() + ": " + msg);
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /msg [Spieler] [Nachricht]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
