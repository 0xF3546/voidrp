package de.polo.core.faction.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FactionType {
    NEUTRAL("Neutral"),
    GOVERNMENT("Staat"),
    GANG("Gang"),
    MAFIA("Mafia"),
    CARTEL("Kartell");

    private final String name;
}
