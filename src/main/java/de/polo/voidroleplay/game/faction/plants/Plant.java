package de.polo.voidroleplay.game.faction.plants;

import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.utils.enums.Drug;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

import java.time.LocalDateTime;

public class Plant {
    @Getter
    private final FactionData planter;

    @Getter
    private final LocalDateTime planted;

    @Getter
    private final Block block;

    @Getter
    private final Drug drug;

    @Getter
    @Setter
    private int water;

    @Getter
    @Setter
    private int fertilizer;

    @Getter
    @Setter
    private int yield;

    public Plant(FactionData planter, LocalDateTime planted, Block block, Drug drug) {
        this.planter = planter;
        this.planted = planted;
        this.block = block;
        this.drug = drug;
    }
}
