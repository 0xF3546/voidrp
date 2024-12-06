package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class GeworbenCommand implements CommandExecutor {
    public GeworbenCommand() {
        Main.registerCommand("geworben", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /geworben [Spieler]");
            return false;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(Prefix.ERROR + "Du kannst dich nicht selbst werben.");
            return false;
        }
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT geworben FROM players WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            if (result.getString("geworben") != null) {
                player.sendMessage(Prefix.ERROR + "Du hast bereits einen Spieler angegeben.");
                return false;
            }
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() == null) continue;
                if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                    player.sendMessage(Prefix.MAIN + "Aktion erfolgreich!");
                    statement = connection.prepareStatement("UPDATE players SET geworben = ? WHERE uuid = ?");
                    statement.setString(1, offlinePlayer.getUniqueId().toString());
                    statement.setString(2, player.getUniqueId().toString());
                    statement.executeUpdate();
                    return false;
                }
            }
        }
        player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
        return false;
    }
}
