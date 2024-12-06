package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrennenCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public TrennenCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("trennen", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.getRelationShip().isEmpty()) {
            for (Map.Entry<String, String> entry : playerData.getRelationShip().entrySet()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                if (offlinePlayer.isOnline()) {
                    Player targetplayer = Bukkit.getPlayer(offlinePlayer.getName());
                    targetplayer.sendMessage("§c" + player.getName() + " hat sich von dir getrennt...");
                    PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                    targetplayerData.setRelationShip(new HashMap<>());
                }
                playerData.setRelationShip(new HashMap<>());
                player.sendMessage("§cDu hast dich von " + offlinePlayer.getName() + " getrennt...");
                try {
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `relationShip` = '{}' WHERE `uuid` = '" + player.getUniqueId() + "'");
                    statement.executeUpdate("UPDATE `players` SET `relationShip` = '{}' WHERE `uuid` = '" + offlinePlayer.getUniqueId() + "'");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        } else {
            player.sendMessage(Main.error + "Du bist in keiner Beziehung.");
        }
        return false;
    }
}
