package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.MySQl.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class PhoneUtils {
    public static HashMap<String, Boolean> phoneCallIsCreated = new HashMap<String, Boolean>();
    public static HashMap<String, String> phoneCallConnection = new HashMap<String, String>();
    public static HashMap<String, Boolean> isInCallConnection = new HashMap<String, Boolean>();

    public static boolean inPhoneCall(Player player) {
        return phoneCallIsCreated.get(player.getUniqueId().toString()) != null;
    }
    public static void createTicket(Player player, String reason) {
        phoneCallIsCreated.put(player.getUniqueId().toString(), true);
    }

    public static void deleteTicket(Player player) {
        if (inPhoneCall(player)) {
            phoneCallIsCreated.remove(player.getUniqueId().toString());
        }
    }

    public static void createTicketConnection(Player player, Player targetplayer) {
        phoneCallConnection.put(player.getUniqueId().toString(), targetplayer.getUniqueId().toString());
        phoneCallConnection.put(targetplayer.getUniqueId().toString(), player.getUniqueId().toString());
        isInCallConnection.put(player.getUniqueId().toString(), true);
        isInCallConnection.put(targetplayer.getUniqueId().toString(), true);
    }
    public static void deleteTicketConnection(Player player, Player targetplayer) {
        phoneCallConnection.remove(player.getUniqueId().toString());
        phoneCallConnection.remove(targetplayer.getUniqueId().toString());
        deleteTicket(targetplayer);
        isInCallConnection.remove(targetplayer.getUniqueId().toString());
        isInCallConnection.remove(player.getUniqueId().toString());
    }
    public static String getConnection(Player player) {
        return phoneCallConnection.get(player.getUniqueId().toString());
    }

    public static boolean isInConnection(Player player) {
        return isInCallConnection.get(player.getUniqueId().toString()) != null;
    }

    public static void addNumberToContacts(Player player, Player targetplayer) throws SQLException {
        String uuid = player.getUniqueId().toString();
        Statement statement = MySQL.getStatement();
        PlayerData playerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
        statement.executeQuery("INSERT INTO `phone_contacts` (`uuid`, `contact_name`, `contact_number`, `contact_uuid`) VALUES ('" + player.getUniqueId().toString() + "', '" + targetplayer.getName() + "', " + playerData.getNumber() + ", '" + targetplayer.getUniqueId().toString() + "')");
    }

    public static void callNumber(Player player, int number) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (PlayerManager.playerDataMap.get(players.getUniqueId().toString()).getNumber() == number) {
                //todo spieler in pre-connection setzen und message senden
            }
        }
    }

    public static void acceptCall(Player player) {
        //todo connection erstellen und messages senden
    }

    public static void denyCall(Player player) {
        //todo pre-connection löschen und messages senden
    }

}
