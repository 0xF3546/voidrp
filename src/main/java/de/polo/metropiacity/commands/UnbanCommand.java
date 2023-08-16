package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;

public class UnbanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.getPlayerData(player);
        String syntax_error = Main.admin_error + "Syntax-Fehler: /unban [Spieler]";
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(syntax_error);
            return false;
        }
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM player_bans WHERE LOWER(name) = '" + args[0].toLowerCase() + "'");
            if (!res.next()) {
                player.sendMessage(Main.error + "Der Spieler wurde nicht in der Banlist gefudnen.");
                return false;
            }
            ADutyCommand.send_message(player.getName() + " hat " + res.getString(3) + " entbannt.", ChatColor.RED);
            player.sendMessage(Main.admin_prefix + "Du hast " + res.getString(3) + " entbannt.");
            statement.execute("DELETE FROM player_bans WHERE LOWER(name) = '" + args[0].toLowerCase() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
