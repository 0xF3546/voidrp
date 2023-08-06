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
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        String syntax_error = Main.admin_error + "Syntax-Fehler: /unban [uuid/name] [Wert]";
        if (playerData.getPermlevel() >= 70) {
            if (args.length >= 2) {
                try {
                    Statement statement = MySQL.getStatement();
                    if (args[0].equalsIgnoreCase("uuid")) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
                        Bukkit.getBanList(BanList.Type.NAME).pardon(Objects.requireNonNull(offlinePlayer.getName()));
                        ADutyCommand.send_message(player.getName() + " hat  §l" + offlinePlayer.getName() + "§7 entbannt.", ChatColor.RED);
                        player.sendMessage(Main.admin_prefix + "Du hast §l" + offlinePlayer.getName() + "§7 entbannt.");
                    } else if (args[0].equalsIgnoreCase("name")) {
                        ResultSet res = statement.executeQuery("SELECT `uuid` FROM `players` WHERE `player_name` = '" + args[1] + "'");
                        if (res.next()) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(res.getString(1)));
                            Bukkit.getBanList(BanList.Type.NAME).pardon(Objects.requireNonNull(offlinePlayer.getName()));
                            ADutyCommand.send_message(player.getName() + " hat " + offlinePlayer.getName() + " entbannt.", ChatColor.RED);
                            player.sendMessage(Main.admin_prefix + "Du hast §l" + offlinePlayer.getName() + "§7 entbannt.");
                        } else {
                            player.sendMessage(Main.admin_error + "Spieler konnte nicht gefunden werden.");
                        }
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
