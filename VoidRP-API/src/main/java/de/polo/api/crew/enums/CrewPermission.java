package de.polo.api.crew.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum CrewPermission {
    BOSS("§8» §cBoss", "Boss", "boss"),
    INVITE("§8» §cEinladen", "Einladen", "invite"),
    KICK("§8» §cRausschmeißen", "Rausschmeißen", "kick"),
    RANK("§8» §cRänge erstellen", "Ränge erstellen", "rank"),
    EDIT_CREW("§8» §cCrew bearbeiten", "Crew bearbeiten", "crew"),
    PERMISSION("§8» §cBerechtigungen bearbeiten", "Berechtigungen bearbeiten", "permission");

    private final String displayName;
    private final String name;
    private final String permission;
}
