package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.*;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UninviteCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;
    public UninviteCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        Main.registerCommand("uninvite", this);
        Main.addTabCompeter("uninvite", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFactionGrade() < 5) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /uninvite [Spieler]");
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden!");
            return false;
        }
        PlayerData targetData = factionManager.getFactionOfPlayer(offlinePlayer.getUniqueId());
        if (!playerData.getFaction().equalsIgnoreCase(targetData.getFaction())) {
            player.sendMessage(Main.error + offlinePlayer.getName() + " ist nicht in deiner Fraktion.");
            return false;
        }
        if (targetData.getFactionGrade() > playerData.getFactionGrade()) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        adminManager.send_message(player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion \"" + targetData.getFaction() + "\" geworfen.", ChatColor.DARK_PURPLE);
        factionManager.removePlayerFromFrak(offlinePlayer.getUniqueId());
        factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion geworfen!");
        if (offlinePlayer.isOnline()) {
            Player target = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            if (target == null) return false;
            target.sendMessage("§8 » §7Du wurdest von " + player.getName() + " aus der Fraktion geworfen!");
        }
        return false;
    }

    @SneakyThrows
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            PlayerData playerData = playerManager.getPlayerData((Player) sender);
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT player_name FROM players WHERE faction = ?");
            statement.setString(1, playerData.getFaction());
            ResultSet result = statement.executeQuery();
            List<String> names = new ArrayList<>();
            while (result.next()) {
                names.add(result.getString("player_name"));
            }
            connection.close();
            statement.close();
            result.close();
            return names;
        }
        return null;
    }
}
