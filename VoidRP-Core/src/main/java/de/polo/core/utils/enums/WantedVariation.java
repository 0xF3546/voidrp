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
    STELLUNG("Stellung", -5);

    private final String name;
    private final int wantedAmount;
}
