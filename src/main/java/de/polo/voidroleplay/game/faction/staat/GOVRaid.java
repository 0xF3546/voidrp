package de.polo.voidroleplay.game.faction.staat;

import de.polo.voidroleplay.dataStorage.FactionData;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class GOVRaid {

    @Getter
    @Setter
    private LocalDateTime started;

    @Getter
    @Setter
    private FactionData defender;

    @Getter
    @Setter
    private boolean open;
}
