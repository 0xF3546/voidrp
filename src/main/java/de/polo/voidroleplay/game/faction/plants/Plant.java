package de.polo.voidroleplay.game.faction.plants;

import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.enums.PlantType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;

public class Plant {
    @Getter
    private final FactionData planter;

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

    public Plant(FactionData planter, LocalDateTime planted, Block block, PlantType plantType) {
        this.planter = planter;
        this.planted = planted;
        this.block = block;
        this.type = plantType;
        time = type.getTime();
    }
}
