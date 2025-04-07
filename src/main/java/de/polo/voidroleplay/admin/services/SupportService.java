package de.polo.voidroleplay.admin.services;

import de.polo.voidroleplay.storage.Ticket;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface SupportService {
    /**
     * Checks if a player has already created a ticket
     */
    boolean ticketCreated(Player player);

    /**
     * Creates a new ticket asynchronously
     */
    CompletableFuture<Ticket> createTicketAsync(Player player, String reason);

    /**
     * Gets all current tickets
     */
    Collection<Ticket> getTickets();

    /**
     * Deletes a player's ticket
     */
    void deleteTicket(Player player) throws Exception;

    /**
     * Removes a specific ticket
     */
    void removeTicket(Ticket ticket);

    /**
     * Creates a connection between two players for a ticket
     */
    void createTicketConnection(Player player, Player targetplayer);

    /**
     * Deletes a ticket connection between two players
     */
    boolean deleteTicketConnection(Player player, Player targetplayer);

    /**
     * Gets a player's ticket (either as creator or editor)
     */
    Ticket getTicket(Player player);

    /**
     * Gets all players involved in a ticket
     */
    List<Player> getPlayersInTicket(Ticket ticket);

    /**
     * Checks if a player is in any ticket connection
     */
    boolean isInConnection(Player player);

    /**
     * Checks if a player is in an accepted ticket
     */
    boolean isInAcceptedTicket(Player player);
}
