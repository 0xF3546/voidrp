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
public enum Powerup {

    EXP(0, 3, 25000, 50000, -1),
    TAX(0, 3, 25000, 25000, -1),
    STORAGE(27, 9, 75000, 200000, 54),
    FISHING(2, 1, 50000, 25000, -1);

    private final int baseAmount;
    private final int upgradeAmount;
    private final int baseUpgradePrice;
    private final int increaseAmount;
    private final int maxAmount;
}
