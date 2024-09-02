package de.polo.voidroleplay.dataStorage;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;

import java.time.LocalDateTime;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Molotov {

    @Getter
    private final LocalDateTime thrownTime;

    @Getter
    private final Item droppedItem;

    public Molotov(LocalDateTime thrownTime, Item droppedItem) {
        this.thrownTime = thrownTime;
        this.droppedItem = droppedItem;
    }
}
