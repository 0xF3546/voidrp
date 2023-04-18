package de.polo.void_roleplay.Utils;

import org.bukkit.entity.Player;

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
}
