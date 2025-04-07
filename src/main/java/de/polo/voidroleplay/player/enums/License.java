package de.polo.voidroleplay.player.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum License {
    WEAPON("Waffenschein"),
    DRIVER("Führerschein"),
    LAWYER("Anwaltszulassung"),
    REAL_ESTATE_BROKER("Maklerzulassung");
    private final String name;
}
