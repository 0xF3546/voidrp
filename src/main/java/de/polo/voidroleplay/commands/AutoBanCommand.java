package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class AutoBanCommand implements CommandExecutor, TabCompleter {
    public static List<String> banReasons = new ObjectArrayList<>();
    private final PlayerManager playerManager;

    public AutoBanCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM banreasons");
            while (res.next()) {
                banReasons.add(res.getString(2));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Main.registerCommand("autoban", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
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
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    ResultSet res = statement.executeQuery("SELECT * FROM banreasons WHERE LOWER(reason) = '" + args[1].toLowerCase() + "'");
                    if (res.next()) {
                        if (res.getInt("amount") == -1) {
                            player.performCommand("permban " + args[0] + " " + res.getString("reason"));
                        } else {
                            player.performCommand("ban name " + args[0] + " " + res.getInt(3) + res.getString(4) + " " + res.getString(2));
                        }
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
