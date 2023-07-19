package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckInvCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 60) {
            if (playerData.isAduty()) {
                if (args.length >= 1) {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                    player.openInventory(targetplayer.getInventory());
                } else {
                    player.sendMessage(Main.admin_error + "Syntax-Fehler: /checkinv [Spieler]");
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
