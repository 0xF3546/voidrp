package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.DBPlayerData;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class UninviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFactionGrade() < 7) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /uninvite [Spieler]");
            return false;
        }
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                if (dbPlayerData.getFaction().equals(playerData.getFaction())) {
                    if (dbPlayerData.getFaction_grade() < playerData.getFactionGrade()) {
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
                        FactionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion geworfen!");
                        Aduty.send_message(player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion \"" + dbPlayerData.getFaction() + "\" geworfen.");
                    } else {
                        player.sendMessage(Main.error_nopermission);
                    }
                } else {
                    player.sendMessage(Main.error + offlinePlayer.getName() + " ist nicht in deiner Fraktion.");
                }
                return true;
            }
        }
        player.sendMessage(Main.error + args[0] + " wurde nicht gefunden.");
        return false;
    }
}
