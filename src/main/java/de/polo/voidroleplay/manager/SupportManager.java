package de.polo.voidroleplay.manager;

import de.polo.voidroleplay.dataStorage.Ticket;
import de.polo.voidroleplay.database.MySQL;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SupportManager {
    public static final List<String> playerTickets = new ArrayList<>();
    public static int TicketCount = 0;
    private final MySQL mySQL;
    private final List<Ticket> Tickets = new ArrayList<>();

    public SupportManager(MySQL mySQL) {
        this.mySQL = mySQL;
    }

    public boolean ticketCreated(Player player) {
        for (Ticket ticket : Tickets) {
            if (ticket.getCreator() == player.getUniqueId()) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public Ticket createTicket(Player player, String reason) {
        Ticket ticket = new Ticket();

        Connection connection = mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO tickets (creator, reason) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);

        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, reason);
        statement.executeUpdate();

        ResultSet generatedKeys = statement.getGeneratedKeys();

        if (generatedKeys.next()) {
            int lastInsertedId = generatedKeys.getInt(1);
            ticket.setId(lastInsertedId);
            System.out.println(lastInsertedId);
        }

        ticket.setCreator(player.getUniqueId());
        ticket.setReason(reason);

        Tickets.add(ticket);

        statement.close();
        connection.close();

        return ticket;
    }

    public Collection<Ticket> getTickets() {
        return Tickets;
    }

    @SneakyThrows
    public void deleteTicket(Player player) {
        for (Ticket ticket : Tickets) {
            if (ticket.getCreator() == player.getUniqueId()) {
                Statement statement = mySQL.getStatement();
                statement.executeUpdate("DELETE FROM tickets WHERE id = " + ticket.getId());
                Tickets.remove(ticket);
                return;
            }
        }
    }

    public void removeTicket(Ticket ticket) {
        Tickets.remove(ticket);
    }

    public void createTicketConnection(Player player, Player targetplayer) {
        for (Ticket ticket : Tickets) {
            if (ticket.getCreator() == player.getUniqueId()) {
                ticket.addEditor(targetplayer.getUniqueId());
            }
        }
    }

    public boolean deleteTicketConnection(Player player, Player targetplayer) {
        Ticket ticket = getTicket(player);
        Ticket ticket2 = getTicket(targetplayer);
        if (ticket != null) {
            Tickets.remove(ticket);
        }
        if (ticket2 != null) {
            Tickets.remove(ticket2);
        }
        return true;
    }

    public Ticket getTicket(Player player) {
        for (Ticket ticket : Tickets) {
            if (ticket.getCreator() == player.getUniqueId()) {
                return ticket;
            }
            for (UUID uuid : ticket.getEditors()) {
                if (uuid == player.getUniqueId()) {
                    return ticket;
                }
            }
        }
        return null;
    }

    public List<Player> getPlayersInTicket(Ticket ticket) {
        List<Player> players = new ArrayList<>();

        players.add(Bukkit.getPlayer(ticket.getCreator()));

        for (UUID uuid : ticket.getEditors()) {
            players.add(Bukkit.getPlayer(uuid));
        }

        return players;
    }

    public boolean isInConnection(Player player) {
        return getTicket(player) != null;
    }
}
