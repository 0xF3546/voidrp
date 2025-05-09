package de.polo.core.manager;

import de.polo.core.Main;
import de.polo.core.database.impl.CoreDatabase;
import de.polo.core.storage.Ticket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SupportManager {
    public static final List<String> playerTickets = new ObjectArrayList<>();
    public static int TicketCount = 0;
    private final CoreDatabase coreDatabase;
    private final List<Ticket> Tickets = new ObjectArrayList<>();

    public SupportManager(CoreDatabase coreDatabase) {
        this.coreDatabase = coreDatabase;
    }

    public boolean ticketCreated(Player player) {
        for (Ticket ticket : Tickets) {
            if (ticket.getCreator() == player.getUniqueId()) {
                return true;
            }
        }
        return false;
    }

    public CompletableFuture<Ticket> createTicketAsync(Player player, String reason) {
        Ticket ticket = new Ticket();
        ticket.setCreator(player.getUniqueId());
        ticket.setReason(reason);

        return Main.getInstance().getCoreDatabase().insertAndGetKeyAsync(
                "INSERT INTO tickets (creator, reason) VALUES (?, ?)",
                player.getUniqueId().toString(),
                reason
        ).thenApply(optionalKey -> {
            if (optionalKey.isPresent()) {
                ticket.setId(optionalKey.get());
                Tickets.add(ticket);
            } else {
                throw new RuntimeException("Failed to create ticket: no ID returned from database.");
            }
            return ticket;
        }).exceptionally(ex -> {
            ex.printStackTrace(); // Log the error for debugging
            throw new RuntimeException("Failed to create ticket", ex);
        });
    }

    public Collection<Ticket> getTickets() {
        return Tickets;
    }

    @SneakyThrows
    public void deleteTicket(Player player) {
        for (Ticket ticket : Tickets) {
            if (ticket.getCreator() == player.getUniqueId()) {
                Statement statement = coreDatabase.getStatement();
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
        List<Player> players = new ObjectArrayList<>();

        players.add(Bukkit.getPlayer(ticket.getCreator()));

        for (UUID uuid : ticket.getEditors()) {
            players.add(Bukkit.getPlayer(uuid));
        }

        return players;
    }

    public boolean isInConnection(Player player) {
        return getTicket(player) != null;
    }

    public boolean isInAcceptedTicket(Player player) {
        Ticket ticket = getTicket(player);
        if (ticket == null) return false;
        return !ticket.getEditors().isEmpty();
    }
}
