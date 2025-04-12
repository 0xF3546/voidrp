package de.polo.api.news;

import de.polo.api.player.VoidPlayer;

import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Advertisement {
    VoidPlayer getPublisher();
    String getContent();
    UUID getUuid();
}
