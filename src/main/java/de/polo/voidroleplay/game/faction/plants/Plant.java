package de.polo.voidroleplay.game.faction.plants;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.utils.enums.Drug;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
