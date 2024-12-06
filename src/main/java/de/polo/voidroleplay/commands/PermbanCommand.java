package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.PlayerManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PermbanCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public PermbanCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("permban", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Main.error + "Syntax-Fehler: /permban [Spieler] [Grund]");
            return false;
        }
        OfflinePlayer target = null;
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() == null) continue;
            if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                target = offlinePlayer;
                break;
            }
        }
        if (target == null) {
            player.sendMessage(Main.error + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        StringBuilder reason = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            reason.append(" ").append(args[i]);
        }
        Bukkit.broadcastMessage(ChatColor.RED + playerData.getRang() + " " + player.getName() + " hat " + target.getName() + " permanent gebannt. Grund: " + reason);
        adminManager.send_message(player.getName() + " hat " + target.getName() + " Permanent gebannt.", ChatColor.RED);
        if (target.isOnline()) {
            Player targetOnPlayer = Bukkit.getPlayer(target.getUniqueId());
            targetOnPlayer.kickPlayer("§8• §6§lVoidRoleplay §8•\n\n§cDu wurdest Permanent vom Server gebannt.\nGrund§8:§7 " + reason + "\n\n§8• §6§lVoidRoleplay §8•");
        }
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO player_bans (uuid, name, reason, punisher, isPermanent) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, target.getUniqueId().toString());
        statement.setString(2, target.getName());
        statement.setString(3, reason.toString());
        statement.setString(4, player.getName());
        statement.setInt(5, 1);
        statement.execute();
        statement.close();

        adminManager.insertNote("System", target.getUniqueId().toString(), "Spieler wurde gebannt (" + reason + ")");
        return false;
    }
}
