package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum HealthInsurance {
    BASIC("Standard", 0, 0),
    PLUS("Plus", 50, 50),
    FULL("Komplett", 100, 125);
    private final String name;
    private final int coverage;
    private int price;
}
