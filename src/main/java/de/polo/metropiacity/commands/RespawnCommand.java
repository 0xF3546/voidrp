package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.DeathUtils;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /respawn [Spieler]");
            return false;
        } else {
            OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                player.sendMessage(Main.error + args[0] + " wurde nicht gefunden.");
                return false;
            }
            if (!offlinePlayer.isOnline()) {
                player.sendMessage(Main.error + offlinePlayer.getName() + " ist nicht online.");
                return false;
            }
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        targetplayer.sendMessage(Main.prefix + "§a" + player.getName() + " hat dich Respawnt!");
        ADutyCommand.send_message(player.getName() + " hat " + targetplayer.getName() + " respawnt.");
        PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
        if (targetplayerData.getFaction() != null) {
            LocationManager.useLocation(targetplayer, targetplayerData.getFaction());
        } else {
            LocationManager.useLocation(targetplayer, "Stadthalle");
        }
        DeathUtils.RevivePlayer(targetplayer);
        return false;
    }
}
