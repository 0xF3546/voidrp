package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.polo.core.Main.database;

public class CPCommand implements CommandExecutor {
    public CPCommand() {
        Main.registerCommand("cp", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 2) {
            database.updateAsync("UPDATE players SET password = ? WHERE uuid = ?", args[1], player.getUniqueId().toString());
            player.sendMessage(Prefix.MAIN + "§aDein Controlpanel-Zugang wurde geupdated.");
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /cp [Passwort]");
        }
        return false;
    }
}
