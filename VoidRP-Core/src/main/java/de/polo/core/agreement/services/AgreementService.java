package de.polo.core.agreement.services;

import de.polo.api.player.VoidPlayer;
import de.polo.core.storage.Agreement;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AgreementService {
    /**
     * Sets a contract between two players
     */
    boolean setVertrag(VoidPlayer player, VoidPlayer target, String type, Object vertrag);

    /**
     * Deletes a contract for a player
     */
    void deleteVertrag(VoidPlayer player);

    /**
     * Sets an agreement between two players
     */
    void setAgreement(VoidPlayer player, VoidPlayer target, Agreement agreement);

    /**
     * Accepts a contract for a player
     */
    void acceptVertrag(VoidPlayer player);

    /**
     * Denies a contract for a player
     */
    void denyVertrag(VoidPlayer player);

    /**
     * Sends an info message with accept/deny options to a player
     */
    void sendInfoMessage(VoidPlayer player);
}