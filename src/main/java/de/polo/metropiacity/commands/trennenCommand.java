package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class trennenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (!playerData.getRelationShip().isEmpty()) {
            for (Map.Entry<String, String> entry: playerData.getRelationShip().entrySet())
            {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                if (offlinePlayer.isOnline()) {
                    Player targetplayer = Bukkit.getPlayer(offlinePlayer.getName());
                    targetplayer.sendMessage("§c" + player.getName() + " hat sich von dir getrennt...");
                    PlayerData targetplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
                    targetplayerData.setRelationShip(new HashMap<>());
                }
                playerData.setRelationShip(new HashMap<>());
                player.sendMessage("§cDu hast dich von " + offlinePlayer.getName() + " getrennt...");
                try {
                    Statement statement = MySQL.getStatement();
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
