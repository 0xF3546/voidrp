package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.StaatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.swing.plaf.nimbus.State;
import java.sql.SQLException;
import java.sql.Statement;

public class registerhouseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 90) {
            if (args.length >= 1) {
                try {
                    Statement statement = MySQL.getStatement();
                    statement.execute("INSERT INTO `housing` (`number`, `price`) VALUES (" + args[0] + ", " + args[1] + ")");
                    player.sendMessage(Main.gamedesign_prefix + "Haus " + args[0] + " wurde mit Preis " + args[1] + " angelegt.");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.gamedesign_prefix + "Syntax-Fehler: /registerhouse [Nummer] [Preis]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
