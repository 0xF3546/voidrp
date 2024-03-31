package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.DBPlayerData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.AdminManager;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.ServerManager;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        if (playerData.getFactionGrade() < 7) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /uninvite [Spieler]");
            return false;
        }
        for (DBPlayerData dbPlayerData : ServerManager.dbPlayerDataMap.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(dbPlayerData.getUuid()));
            if (offlinePlayer.getName() != null) {
                if (offlinePlayer.getName().equalsIgnoreCase(args[0])) {
                    if (dbPlayerData.getFaction().equals(playerData.getFaction())) {
                        if (dbPlayerData.getFaction_grade() < playerData.getFactionGrade()) {
                            adminManager.send_message(player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion \"" + dbPlayerData.getFaction() + "\" geworfen.", ChatColor.DARK_PURPLE);
                            if (offlinePlayer.isOnline()) {
                                try {
                                    factionManager.removePlayerFromFrak(offlinePlayer.getPlayer());
                                    Player target = (Player) offlinePlayer;
                                    target.sendMessage("§8 » §7Du wurdest von " + player.getName() + " aus der Fraktion geworfen!");
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
                            factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat " + offlinePlayer.getName() + " aus der Fraktion geworfen!");
                        } else {
                            player.sendMessage(Main.error_nopermission);
                        }
                    } else {
                        player.sendMessage(Main.error + offlinePlayer.getName() + " ist nicht in deiner Fraktion.");
                    }
                    return true;
                }
            }
        }
        player.sendMessage(Main.error + args[0] + " wurde nicht gefunden.");
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
