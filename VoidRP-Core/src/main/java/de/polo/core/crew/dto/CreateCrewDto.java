package de.polo.core.crew.dto;

import lombok.Getter;

import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateCrewDto {
    @Getter
    private final String name;

    @Getter
    private final UUID owner;

    public CreateCrewDto(final String name, final UUID owner) {
        this.name = name;
        this.owner = owner;
    }
}
