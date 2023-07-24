package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

import static org.bukkit.Bukkit.getServer;

public class SetTeamCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (player.hasPermission("operator")) {
            if (args.length == 2) {
                Player targetplayer = getServer().getPlayer(args[0]);
                String uuid = targetplayer.getUniqueId().toString();
                PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
                String rank = args[1];
                if (ServerManager.rankDataMap.get(rank) == null) {
                    player.sendMessage(Main.error + "Rang nicht gefunden.");
                    return false;
                }
                player.sendMessage(Main.admin_prefix + targetplayer.getName() + " ist nun §c" + rank + "§7.");
                targetplayer.sendMessage(Main.admin_prefix + "Du bist nun §c" + rank + "§7!");
                try {
                    Statement statement = MySQL.getStatement();
                    statement.executeUpdate("UPDATE players SET rankDuration = null WHERE uuid = '" + targetplayer.getUniqueId() + "'");
                    targetplayer.sendMessage("§b   Info§8:§f Da du nun Teammitglied bist, hast du deine Spielerränge verloren.");
                    playerData.setRankDuration(null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                PlayerManager.setRang(targetplayer.getUniqueId().toString(), rank);
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /setgroup [Spieler] [Rang]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
