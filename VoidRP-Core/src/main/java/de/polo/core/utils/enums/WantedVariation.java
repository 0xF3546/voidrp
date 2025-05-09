package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@Getter
@AllArgsConstructor
public enum WantedVariation {
    STELLUNG("Stellung", -5),
    GUTE_FUEHRUNG("Gute Führung", -5),
    SCHLECHTE_FUEHRUNG("Schlechte Führung", 5),
    FUEHRERSCHEIN_ABNAHME("Führerscheinabnahme", 0),
    ;

    private final String name;
    private final int wantedAmount;
}
