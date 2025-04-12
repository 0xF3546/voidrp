package de.polo.core.news.entities;

import de.polo.api.news.Advertisement;
import de.polo.api.player.VoidPlayer;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreAdvertisement implements Advertisement {
    @Getter
    private final VoidPlayer publisher;
    @Getter
    private final String content;

    @Getter
    private final UUID uuid;

    public CoreAdvertisement(VoidPlayer publisher, String content) {
        this.publisher = publisher;
        this.content = content;
        this.uuid = UUID.randomUUID();
    }
}
