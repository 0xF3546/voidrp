package de.polo.voidroleplay.agreement.services;

import de.polo.voidroleplay.storage.Agreement;
import org.bukkit.entity.Player;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AgreementService {
    /**
     * Sets a contract between two players
     */
    boolean setVertrag(Player player, Player target, String type, Object vertrag);

    /**
     * Deletes a contract for a player
     */
    void deleteVertrag(Player player);

    /**
     * Sets an agreement between two players
     */
    void setAgreement(Player player, Player target, Agreement agreement);

    /**
     * Accepts a contract for a player
     */
    void acceptVertrag(Player player);

    /**
     * Denies a contract for a player
     */
    void denyVertrag(Player player);

    /**
     * Sends an info message with accept/deny options to a player
     */
    void sendInfoMessage(Player player);
}