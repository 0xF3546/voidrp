package de.polo.voidroleplay.dataStorage;

import lombok.Getter;
import org.bukkit.block.Block;

import java.time.LocalDateTime;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Molotov {
    @Getter
    private final LocalDateTime thrown;

    @Getter
    private final Block block;

    public Molotov(LocalDateTime thrown, Block block) {
        this.thrown = thrown;
        this.block = block;
    }
}
