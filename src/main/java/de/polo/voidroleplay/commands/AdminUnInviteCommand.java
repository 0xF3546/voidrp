package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.DBPlayerData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class AdminUnInviteCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;
    private final Utils utils;
    public AdminUnInviteCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("adminuninvite", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isAduty()) {
            player.sendMessage(Prefix.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Prefix.admin_error + "Syntax-Fehler: /auninvite [Spieler]");
            return false;
        }
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        OfflinePlayer offlinePlayer = utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Prefix.ERROR + args[0] + " wurde nicht gefunden.");
            return false;
        }
        DBPlayerData dbPlayerData = ServerManager.dbPlayerDataMap.get(offlinePlayer.getUniqueId().toString());
        if (offlinePlayer.getName() == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
            adminManager.send_message(player.getName() + " hat " + offlinePlayer.getName() + " Administrativ aus der Fraktion \"" + dbPlayerData.getFaction() + "\" geworfen.", ChatColor.DARK_PURPLE);
            if (offlinePlayer.isOnline()) {
                try {
                    factionManager.removePlayerFromFrak(offlinePlayer.getPlayer());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    factionManager.removeOfflinePlayerFromFrak(offlinePlayer);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            player.sendMessage(Prefix.admin_error + "Du hast " + offlinePlayer.getName() + " aus der Fraktion geworfen.");
            return true;
        }
        player.sendMessage(Prefix.ERROR + args[0] + " wurde nicht gefunden.");
        return false;
    }
}
