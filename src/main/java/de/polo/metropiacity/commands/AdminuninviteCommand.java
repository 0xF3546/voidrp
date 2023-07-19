package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.DBPlayerData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.ServerManager;
import de.polo.metropiacity.Utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class AdminuninviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (!playerData.isAduty()) {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.admin_error + "Syntax-Fehler: /auninvite [Spieler]");
            return false;
        }
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Main.error + args[0] + " wurde nicht gefunden.");
            return false;
        }
        DBPlayerData dbPlayerData = ServerManager.dbPlayerDataMap.get(offlinePlayer.getUniqueId().toString());
        if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
            if (offlinePlayer.isOnline()) {
                try {
                    FactionManager.removePlayerFromFrak(offlinePlayer.getPlayer());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    FactionManager.removeOfflinePlayerFromFrak(offlinePlayer);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            Aduty.send_message(player.getName() + " hat " + offlinePlayer.getName() + " Administrativ aus der Fraktion \"" + dbPlayerData.getFaction() + "\" geworfen.");
            player.sendMessage(Main.admin_prefix + "Du hast " + offlinePlayer.getName() + " aus der Fraktion geworfen.");
            return true;
        }
        player.sendMessage(Main.error + args[0] + " wurde nicht gefunden.");
        return false;
    }
}
