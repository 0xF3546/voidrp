package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPHereCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.admin_error + "Syntax-Fehler: /tphere [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (!targetplayer.isOnline()) {
            player.sendMessage(Main.admin_error + args[0] + " ist nicht online.");
            return false;
        }
        targetplayer.teleport(player.getLocation());
        player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 zu dir teleportiert.");
        targetplayer.sendMessage(Main.prefix + "§c" + playerData.getRang() + " " + player.getName() + "§7 hat dich zu sich teleportiert.");
        ADutyCommand.send_message(player.getName() + " hat " + targetplayer.getName() + " zu sich Teleportiert.", ChatColor.RED);
        return false;
    }
}
