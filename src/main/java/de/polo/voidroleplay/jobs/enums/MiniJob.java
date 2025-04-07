package de.polo.voidroleplay.jobs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum MiniJob {
    WASTE_COLLECTOR("Müllsammler"),
    BOTTLE_TRANSPORT("Flaschenfahrer"),
    SEWER_CLEANER("Kanalreiniger");
    private final String name;
}
