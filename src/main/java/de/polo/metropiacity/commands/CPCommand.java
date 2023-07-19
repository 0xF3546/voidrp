package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class CPCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 2) {
            try {
                Statement statement = MySQL.getStatement();
                statement.executeUpdate("UPDATE `players` SET `email` = '" + args[0] + "', password = '" + args[1] + "' WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                player.sendMessage(Main.prefix + "§aDein Controlpanel-Zugang wurde geupdated.");
            } catch (SQLException e) {
                player.sendMessage(Main.error + "§cEin Fehler ist aufgetreten. Kontaktiere einen Entwickler.");
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /cp [Email] [Passwort]");
        }
        return false;
    }
}
