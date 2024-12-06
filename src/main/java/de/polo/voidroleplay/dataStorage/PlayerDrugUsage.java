package de.polo.voidroleplay.dataStorage;

import de.polo.voidroleplay.utils.enums.Drug;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerDrugUsage {

    @Getter
    private final UUID uuid;

    @Getter
    private final Drug drug;

    @Getter
    private final LocalDateTime usage;

    public PlayerDrugUsage(UUID uuid, Drug drug, LocalDateTime usage) {
        this.uuid = uuid;
        this.drug = drug;
        this.usage = usage;
    }
}
