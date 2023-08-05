package de.polo.metropiacity.commands;

import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.TeamSpeak;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class TSUnlinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getTeamSpeakUID() != null) {
            Client client = TeamSpeak.getTeamSpeak().getAPI().getClientByUId(playerData.getTeamSpeakUID());
            if (client != null) {
                playerData.setTeamSpeakUID(null);
                try {
                    Statement statement = MySQL.getStatement();
                    statement.executeUpdate("UPDATE `players` SET `teamSpeakUID` = " + playerData.getTeamSpeakUID() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage("§8[§3TeamSpeak§8]§b Du bist nun nicht mehr verifiziert.");
                TeamSpeak.getTeamSpeak().getAPI().editClient(client.getId(), ClientProperty.CLIENT_DESCRIPTION, "Spieler ist nicht verifiziert");
                TeamSpeak.getTeamSpeak().removeClientGroups(client);
            } else {
                player.sendMessage(Main.error + "Du bist nicht auf dem TeamSpeak online.");
            }
        } else {
            player.sendMessage(Main.error + "Du bist nicht verifiziert auf dem TeamSpeak.");
        }
        return false;
    }
}
