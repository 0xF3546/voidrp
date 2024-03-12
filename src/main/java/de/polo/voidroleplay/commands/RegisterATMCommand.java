package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.utils.AdminManager;
import de.polo.voidroleplay.utils.PlayerManager;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class RegisterATMCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final MySQL mySQL;
    public RegisterATMCommand(PlayerManager playerManager, AdminManager adminManager, MySQL mySQL) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.mySQL = mySQL;
        Main.registerCommand("registeratm", this);
    }
    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Main.error + "Syntax-Fehler: /registeratm [BlockId(/registerblock atm)] [ATM-Name]");
            return false;
        }
        int blockId = Integer.parseInt(args[0]);
        String atmName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Connection connection = mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO atm (blockId, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, blockId);
        statement.setString(2, atmName);
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            player.sendMessage(Main.admin_prefix + "Du hast einen ATM registriert #" + generatedKeys.getInt(1));
            adminManager.send_message(player.getName() + " hat einen ATM registriert (ATM #" + generatedKeys.getInt(1) + ").", ChatColor.GOLD);
        }

        statement.close();
        connection.close();
        return false;
    }
}
