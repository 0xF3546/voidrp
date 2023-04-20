package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class unbanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        String syntax_error = Main.admin_error + "Syntax-Fehler: /unban [uuid/name] [Wert]";
        if (playerData.getPermlevel() >= 70) {
            if (args.length >= 2) {
                try {
                    Statement statement = MySQL.getStatement();
                    if (args[0].equalsIgnoreCase("uuid")) {
                        statement.execute("DELETE FROM `player_bans` WHERE `uuid` = '" + args[1] + "'");
                        statement.executeUpdate("UPDATE `players` SET `isBanned` = false WHERE `uuid` = '" + args[1] +"'");
                        aduty.send_message(player.getName() + " hat Spieler mit UUID §l" + args[1] + "§7 entbannt.");
                        player.sendMessage(Main.admin_prefix + "Du hast einen Spieler entbannt.");
                    } else if (args[0].equalsIgnoreCase("name")) {
                        statement.execute("DELETE FROM `player_bans` WHERE `name` = '" + args[1] + "'");
                        statement.executeUpdate("UPDATE `players` SET `isBanned` = false WHERE `player_name` = '" + args[1] +"'");
                        aduty.send_message(player.getName() + " hat Spieler mit Name §l" + args[1] + "§7 entbannt.");
                        player.sendMessage(Main.admin_prefix + "Du hast einen Spieler entbannt.");
                    } else {
                        player.sendMessage(syntax_error);
                    }
                } catch (SQLException e) {
                    player.sendMessage(Main.error + "MySQL Fehler");
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(syntax_error);
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
