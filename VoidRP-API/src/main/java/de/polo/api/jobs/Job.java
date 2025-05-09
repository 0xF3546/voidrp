package de.polo.api.jobs;

import de.polo.api.player.VoidPlayer;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Job {
    void startJob(VoidPlayer player);

    void endJob(VoidPlayer player);
}
