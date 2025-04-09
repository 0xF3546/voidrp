package de.polo.api.jobs;

import de.polo.api.player.VoidPlayer;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface TransportJob extends Job {
    void handleDrop(VoidPlayer player);
}
