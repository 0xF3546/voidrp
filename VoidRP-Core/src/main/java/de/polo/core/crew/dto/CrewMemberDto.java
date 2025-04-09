package de.polo.core.crew.dto;

import de.polo.api.crew.CrewRank;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CrewMemberDto {
    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    @Getter
    private final CrewRank crewRank;

    public CrewMemberDto(UUID uuid, String name, CrewRank crewRank) {
        this.uuid = uuid;
        this.name = name;
        this.crewRank = crewRank;
    }
}
