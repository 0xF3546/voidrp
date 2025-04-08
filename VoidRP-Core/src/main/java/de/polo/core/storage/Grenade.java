package de.polo.core.storage;

import lombok.Getter;
import org.bukkit.entity.Item;

import java.time.LocalDateTime;

public class Grenade {

    @Getter
    private final LocalDateTime thrownTime;

    @Getter
    private final Item droppedItem;

    public Grenade(LocalDateTime thrownTime, Item droppedItem) {
        this.thrownTime = thrownTime;
        this.droppedItem = droppedItem;
    }
}
