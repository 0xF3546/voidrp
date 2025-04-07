package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.admin.services.impl.AdminManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UnbanCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public UnbanCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("unban", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        String syntax_error = Prefix.ADMIN_ERROR + "Syntax-Fehler: /unban [Spieler]";
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(syntax_error);
            return false;
        }
        try {
            Statement statement = Main.getInstance().coreDatabase.getStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM player_bans WHERE LOWER(name) = '" + args[0].toLowerCase() + "'");
            if (!res.next()) {
                player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht in der Banlist gefudnen.");
                return false;
            }
            adminManager.send_message(player.getName() + " hat " + res.getString(3) + " entbannt.", ChatColor.RED);
            player.sendMessage(Prefix.ADMIN + "Du hast " + res.getString(3) + " entbannt.");
            Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM player_bans WHERE LOWER(name) = ?", args[0].toLowerCase());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
