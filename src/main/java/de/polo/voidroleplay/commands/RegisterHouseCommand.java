package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class RegisterHouseCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public RegisterHouseCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("registerhouse", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 90) {
            if (args.length >= 1) {
                try {
                    Statement statement = Main.getInstance().mySQL.getStatement();
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
