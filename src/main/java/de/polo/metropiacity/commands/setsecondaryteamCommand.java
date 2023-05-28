package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class setsecondaryteamCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 90) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer.isOnline()) {
                    PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                    targetplayerData.setSecondaryTeam(args[1]);
                    targetplayer.sendMessage("§8[§6" + args[1] + "§8]§e " + player.getName() + " hat dich in das Team hinzugefügt.");
                    player.sendMessage("§8[§6" + args[1] + "§8]§e Du hast " + targetplayer.getName() + " in das Team hinzugefügt.");
                    try {
                        Statement statement = MySQL.getStatement();
                        statement.executeUpdate("UPDATE `players` SET `secondaryTeam` = '" + args[1] + "' WHERE `uuid` = '" + targetplayer.getUniqueId().toString() + "'");
                        statement.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage(Main.error + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /setsecondaryteam [Spieler] [Team]");
                player.sendMessage("§8 ➥ §bInfo§8:§f Folgende Teams gibt es: Bau-Team, PR-Team, Event-Team");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
