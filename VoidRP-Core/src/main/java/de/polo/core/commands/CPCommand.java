package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class CPCommand implements CommandExecutor {
    public CPCommand() {
        Main.registerCommand("cp", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 2) {
            try {
                Statement statement = Main.getInstance().coreDatabase.getStatement();
                statement.executeUpdate("UPDATE `players` SET `email` = '" + args[0] + "', password = '" + args[1] + "' WHERE `uuid` = '" + player.getUniqueId() + "'");
                player.sendMessage(Prefix.MAIN + "§aDein Controlpanel-Zugang wurde geupdated.");
            } catch (SQLException e) {
                player.sendMessage(Prefix.ERROR + "§cEin Fehler ist aufgetreten. Kontaktiere einen Entwickler.");
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /cp [Email] [Passwort]");
        }
        return false;
    }
}
