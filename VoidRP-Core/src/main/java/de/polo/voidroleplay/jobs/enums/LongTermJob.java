package de.polo.voidroleplay.jobs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@Getter
@AllArgsConstructor
public enum LongTermJob {
    LAWYER("Anwalt"),
    REAL_ESTATE_BROKER("Makler");
    private final String name;
}
