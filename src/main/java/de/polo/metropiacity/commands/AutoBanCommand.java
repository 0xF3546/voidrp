package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AutoBanCommand implements CommandExecutor, TabCompleter {
    public static ArrayList<String> banReasons = new ArrayList<>();
    public static void init() {
        try {
            Statement statement = MySQL.getStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM banreasons");
            while (res.next()) {
                banReasons.add(res.getString(2));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Main.error + "/aban [Spieler] [Grund]");
            return false;
        }
        for (int i = 0; i < banReasons.size(); i++) {
            if (banReasons.get(i).equalsIgnoreCase(args[1])) {
                try {
                    Statement statement = MySQL.getStatement();
                    ResultSet res = statement.executeQuery("SELECT * FROM banreasons WHERE LOWER(reason) = '" + args[1].toLowerCase() + "'");
                    if (res.next()) {
                        player.performCommand("ban name " + args[0] + " " + res.getInt(3) + res.getString(4) + " " + res.getString(2));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        }
        player.sendMessage(Main.error + "Der Bangrund wurde nicht gefunden.");
        return false;
    }
    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            return banReasons;
        }
        return null;
    }
}
