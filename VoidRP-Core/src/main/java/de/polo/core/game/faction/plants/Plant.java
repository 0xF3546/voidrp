package de.polo.core.game.faction.plants;

import de.polo.core.faction.entity.Faction;
import de.polo.core.utils.enums.PlantType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

import java.time.LocalDateTime;

public class Plant {
    @Getter
    private final Faction planter;

    @Getter
    private final LocalDateTime planted;

    @Getter
    private final Block block;

    @Getter
    private final PlantType type;

    @Getter
    @Setter
    private int water;

    @Getter
    @Setter
    private int fertilizer;

    @Getter
    @Setter
    private int yield;

    @Getter
    @Setter
    private int time;

    @Getter
    @Setter
    private boolean receivedXP = false;

    public Plant(Faction planter, LocalDateTime planted, Block block, PlantType plantType) {
        this.planter = planter;
        this.planted = planted;
        this.block = block;
        this.type = plantType;
        time = type.getTime();
    }
}
