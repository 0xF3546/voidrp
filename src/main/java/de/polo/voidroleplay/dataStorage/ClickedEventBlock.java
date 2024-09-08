package de.polo.voidroleplay.dataStorage;

import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ClickedEventBlock {
    @Getter
    private final int blockId;

    public ClickedEventBlock(int blockId) {
        this.blockId = blockId;
    }
}
