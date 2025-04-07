package de.polo.voidroleplay.agreement.services;

import de.polo.voidroleplay.player.entities.VoidPlayer;
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