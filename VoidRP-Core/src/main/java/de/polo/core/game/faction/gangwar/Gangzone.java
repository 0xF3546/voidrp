package de.polo.core.game.faction.gangwar;

import de.polo.api.gangwar.IGangzone;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Gangzone implements IGangzone {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String owner;

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private Timestamp lastAttack;
}
