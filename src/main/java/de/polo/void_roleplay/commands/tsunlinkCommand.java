package de.polo.void_roleplay.commands;

import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroupClient;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.TeamSpeak;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class tsunlinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getTeamSpeakUID() != null) {
            Client client = TeamSpeak.getAPI().getClientByUId(playerData.getTeamSpeakUID());
            if (client != null) {
                playerData.setTeamSpeakUID(null);
                try {
                    Statement statement = MySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `teamSpeakUID` = '" + playerData.getTeamSpeakUID() + "' WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage("§8[§3TeamSpeak§8]§b Du bist nun nicht mehr verifiziert.");
                TeamSpeak.removeClientGroups(client);
                TeamSpeak.getAPI().editClient(client.getId(), ClientProperty.CLIENT_DESCRIPTION, null);
            } else {
                player.sendMessage(Main.error + "Du bist nicht auf dem TeamSpeak online.");
            }
        } else {
            player.sendMessage(Main.error + "Du bist nicht verifiziert auf dem TeamSpeak.");
        }
        return false;
    }
}
