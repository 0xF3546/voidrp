package de.polo.api.faction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Type classification for a faction.
 *
 * <p>Placed in the API module so domain services can reference it without
 * depending on any Core/Bukkit/Hibernate class.
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
