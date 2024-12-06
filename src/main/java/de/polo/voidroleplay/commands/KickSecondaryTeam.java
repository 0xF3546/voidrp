package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.TeamSpeak;
import de.polo.voidroleplay.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class KickSecondaryTeam implements CommandExecutor {
    private final PlayerManager playerManager;

    public KickSecondaryTeam(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("kicksecondaryteam", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /kicksecondaryteam [Spieler]");
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
            return false;
        }
        if (offlinePlayer.isOnline()) {
            PlayerData target = playerManager.getPlayerData(offlinePlayer.getUniqueId());
            target.setSecondaryTeam(null);
        }
        player.sendMessage(Prefix.MAIN + "Du hast " + offlinePlayer.getName() + " aus seinem Sub-Team gekickt.");
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET secondaryTeam = NULL WHERE uuid = ?");
        statement.setString(1, offlinePlayer.getUniqueId().toString());
        statement.execute();
        connection.close();
        TeamSpeak.reloadPlayer(offlinePlayer.getUniqueId());
        return false;
    }
}
