package de.polo.core.storage;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Item;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Corpse {
    @Getter
    private final Item skull;

    @Getter
    private final UUID uuid;

    @Getter
    private final LocalDateTime despawnTime;

    @Getter
    @Setter
    private boolean isJobActive = false;

    @Getter
    @Setter
    private int price;

    public Corpse(Item skull, UUID uuid, LocalDateTime despawnTime) {
        this.skull = skull;
        this.uuid = uuid;
        this.despawnTime = despawnTime;
    }
}
