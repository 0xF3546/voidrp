package de.polo.core.crew.dto;

import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateCrewRankDto {

    @Getter
    private final String name;
    @Getter
    private final TextColor color;
    @Getter
    private final int rank;
    @Getter
    private final int crewId;
    @Getter
    private final boolean isDefault;
    @Getter
    private final boolean isBoss;

    public CreateCrewRankDto(String name, TextColor color, int rank, int crewId, boolean isDefault, boolean isBoss) {
        this.name = name;
        this.color = color;
        this.rank = rank;
        this.crewId = crewId;
        this.isDefault = isDefault;
        this.isBoss = isBoss;
    }
}
