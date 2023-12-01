package de.polo.metropiacity.utils;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SupportManager {
    public static final HashMap<String, Boolean> ticketIsCreated = new HashMap<>();
    public static final HashMap<String, String> ticketReason = new HashMap<>();
    public static final HashMap<String, String> ticketConnection = new HashMap<>();
    public static final HashMap<String, Boolean> isInConnection = new HashMap<>();
    public static int TicketCount = 0;
    public static final List<String> playerTickets = new ArrayList<>();

    public SupportManager() {

    }

    public boolean ticketCreated(Player player) {
        return ticketIsCreated.get(player.getUniqueId().toString()) != null;
    }

    public void createTicket(Player player, String reason) {
        ticketIsCreated.put(player.getUniqueId().toString(), true);
        ticketReason.put(player.getUniqueId().toString(), reason);
        TicketCount++;
        playerTickets.add(player.getName());
    }

    public void deleteTicket(Player player) {
        if (ticketCreated(player)) {
            ticketReason.remove(player.getUniqueId().toString());
            ticketIsCreated.remove(player.getUniqueId().toString());
            TicketCount--;
            for (int i = 0; i < playerTickets.size(); i++) {
                System.out.println(playerTickets.get(i));
                if (playerTickets.get(i) == player.getName()) {
                    playerTickets.remove(i);
                }
            }
        }
    }

    public void createTicketConnection(Player player, Player targetplayer) {
        ticketConnection.put(player.getUniqueId().toString(), targetplayer.getUniqueId().toString());
        ticketConnection.put(targetplayer.getUniqueId().toString(), player.getUniqueId().toString());
        isInConnection.put(player.getUniqueId().toString(), true);
        isInConnection.put(targetplayer.getUniqueId().toString(), true);
    }
    public boolean deleteTicketConnection(Player player, Player targetplayer) {
        if (ticketConnection.get(player.getUniqueId().toString()) == null || ticketConnection.get(targetplayer.getUniqueId().toString()) == null) {
            return false;
        }
        ticketConnection.remove(player.getUniqueId().toString());
        ticketConnection.remove(targetplayer.getUniqueId().toString());
        deleteTicket(targetplayer);
        isInConnection.remove(targetplayer.getUniqueId().toString());
        isInConnection.remove(player.getUniqueId().toString());
        return true;
    }
    public String getConnection(Player player) {
        return ticketConnection.get(player.getUniqueId().toString());
    }

    public boolean isInConnection(Player player) {
        return isInConnection.get(player.getUniqueId().toString()) != null;
    }
}
