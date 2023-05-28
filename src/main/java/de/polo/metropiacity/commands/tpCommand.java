package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 70) {
            if (playerData.isAduty()) {
                if (args.length > 0) {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                    if (targetplayer.isOnline()) {
                        player.teleport(targetplayer.getLocation());
                        player.sendMessage(Main.admin_prefix + "Du hast dich zu §c" + targetplayer.getName() + "§7 teleportiert.");
                    } else {
                        player.sendMessage(Main.admin_error + args[0] + " ist nicht online.");
                    }
                } else {
                    player.sendMessage(Main.admin_error + "Syntax-Fehler: /tp [Spieler]");
                }
            } else {
                player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
