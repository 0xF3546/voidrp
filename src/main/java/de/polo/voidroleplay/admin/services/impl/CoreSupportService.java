package de.polo.voidroleplay.admin.services.impl;

import de.polo.voidroleplay.admin.services.SupportService;
import de.polo.voidroleplay.storage.Ticket;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static de.polo.voidroleplay.Main.supportManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreSupportService implements SupportService {

    @Override
    public boolean ticketCreated(Player player) {
        return supportManager.ticketCreated(player);
    }

    @Override
    public CompletableFuture<Ticket> createTicketAsync(Player player, String reason) {
        return supportManager.createTicketAsync(player, reason);
    }

    @Override
    public Collection<Ticket> getTickets() {
        return supportManager.getTickets();
    }

    @Override
    public void deleteTicket(Player player) throws Exception {
        supportManager.deleteTicket(player);
    }

    @Override
    public void removeTicket(Ticket ticket) {
        supportManager.removeTicket(ticket);
    }

    @Override
    public void createTicketConnection(Player player, Player targetplayer) {
        supportManager.createTicketConnection(player, targetplayer);
    }

    @Override
    public boolean deleteTicketConnection(Player player, Player targetplayer) {
        return supportManager.deleteTicketConnection(player, targetplayer);
    }

    @Override
    public Ticket getTicket(Player player) {
        return supportManager.getTicket(player);
    }

    @Override
    public List<Player> getPlayersInTicket(Ticket ticket) {
        return supportManager.getPlayersInTicket(ticket);
    }

    @Override
    public boolean isInConnection(Player player) {
        return supportManager.isInConnection(player);
    }

    @Override
    public boolean isInAcceptedTicket(Player player) {
        return supportManager.isInAcceptedTicket(player);
    }
}
